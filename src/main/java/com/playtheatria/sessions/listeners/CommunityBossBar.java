package com.playtheatria.sessions.listeners;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.TheatriaSessions;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.RewardCommunityEvent;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Shows a single server-wide boss bar while the community sell-hand bonus is active.
 *
 * <p>The bonus itself is granted in {@link RewardCommunity}; this just surfaces it. The bar shows
 * the live boost (e.g. {@code +30% sell hand}) and nudges players to {@code /daily-reward} for the
 * full breakdown. It stays up for as long as a bonus is active and comes down at the daily reset
 * when the bonus is revoked. There is one shared {@link BossBar}; Adventure fans every text/fill
 * update out to whichever players are viewing it.
 *
 * <p>Players can opt out for themselves with {@code /daily-reward bossbar off} (and back in with
 * {@code on}). The preference lives in the player's {@link PersistentDataContainer}, so it survives
 * relogs and restarts without any extra storage, and opted-out players are skipped whenever the bar
 * is shown.
 *
 * <p>The active tier is always derived from the persisted reward count
 * ({@link RewardTier#getNearestTier(int)} over {@link DailyStatsService#getRewardsEarned()}), so a
 * mid-day restart re-attaches the correct bar on join without storing any extra state. The bar is
 * only ever shown when both {@code community-rewards-enabled} and {@code community-bonus.boss-bar}
 * are on.
 */
public class CommunityBossBar implements Listener {
    // Mirrors the green/gold styling of the RewardCommunity "Boon" message so the two read alike.
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final String GRADIENT = "<gradient:#67e9a8:#1fa86b>";
    private static final String HL = "<color:#ffd119>";
    private static final String HL_END = "</color>";

    private final ConfigManager cm;
    private final DailyStatsService dailyStatsService;
    private final PLog log;
    // Per-player "I hid the bar" flag, persisted on the player via the data container.
    private final NamespacedKey optOutKey;

    // One shared bar reused for the whole server; we only ever swap its text and fill.
    private final BossBar bar =
            BossBar.bossBar(
                    Component.empty(), 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);

    public CommunityBossBar(
            TheatriaSessions plugin,
            ConfigManager cm,
            DailyStatsService dailyStatsService,
            PLog log) {
        this.cm = cm;
        this.dailyStatsService = dailyStatsService;
        this.log = log;
        this.optOutKey = new NamespacedKey(plugin, "bossbar_opt_out");
    }

    /**
     * Re-attaches (or removes) the bar for everyone currently online. Called on enable so a
     * {@code /reload} mid-bonus restores the bar for players who are already on the server.
     */
    public void refresh() {
        RewardTier tier = activeTier();
        if (tier == null) {
            hideFromAll();
            return;
        }
        applyTier(tier);
        showToAll();
    }

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        if (!isEnabled()) return;
        applyTier(event.getRewardTier());
        showToAll();
        log.debugFmt("[CommunityBossBar] Showing boss bar for %s", event.getRewardTier());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RewardTier tier = activeTier();
        if (tier == null) return;
        Player player = event.getPlayer();
        if (isOptedOut(player)) return;
        // Refresh the text first in case this joiner is the first viewer since a restart.
        applyTier(tier);
        player.showBossBar(bar);
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        // The bonus is revoked at the reset, so the bar must always come down here -- we cannot
        // derive this from the reward count because that may not be zeroed yet at this priority.
        hideFromAll();
        log.debug("[CommunityBossBar] Cleared community boss bar at daily reset.");
    }

    /** Removes the bar from every online player (daily reset and plugin disable). */
    public void hideFromAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bar);
        }
    }

    /** Whether {@code player} has hidden the boss bar for themselves. */
    public boolean isOptedOut(Player player) {
        return player.getPersistentDataContainer()
                        .getOrDefault(optOutKey, PersistentDataType.BYTE, (byte) 0)
                == (byte) 1;
    }

    /**
     * Records the player's boss-bar preference and applies it immediately: hiding the bar when they
     * opt out, or showing the current bonus (if any) when they opt back in.
     */
    public void setOptedOut(Player player, boolean optedOut) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (optedOut) {
            pdc.set(optOutKey, PersistentDataType.BYTE, (byte) 1);
            player.hideBossBar(bar);
            return;
        }
        pdc.remove(optOutKey);
        RewardTier tier = activeTier();
        if (tier != null) {
            applyTier(tier);
            player.showBossBar(bar);
        }
    }

    private void showToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isOptedOut(player)) continue;
            player.showBossBar(bar);
        }
    }

    /** Updates the shared bar's text and fill to reflect the given tier. */
    private void applyTier(RewardTier tier) {
        bar.name(
                MINI.deserialize(
                        GRADIENT
                                + "Community boost: "
                                + HL
                                + "+"
                                + tier.getPercentage()
                                + " sell hand"
                                + HL_END
                                + " — "
                                + HL
                                + "/daily-reward"
                                + HL_END
                                + " to look closer"));
        bar.progress(tierProgress(tier));
    }

    /** Fills the bar in proportion to how high the unlocked tier is (Level 1/5 ... 5/5). */
    private float tierProgress(RewardTier tier) {
        return (float) (tier.ordinal() + 1) / RewardTier.values().length;
    }

    /** The tier whose bonus is currently active, or {@code null} when nothing should be shown. */
    private RewardTier activeTier() {
        if (!isEnabled()) return null;
        switch (RewardTier.getNearestTier(dailyStatsService.getRewardsEarned())) {
            case Ok<RewardTier, Exception> ok -> {
                return ok.value();
            }
            case Err<RewardTier, Exception> ignored -> {
                return null;
            }
        }
    }

    private boolean isEnabled() {
        return cm.isCommunityRewardsEnabled() && cm.isCommunityBossBarEnabled();
    }
}
