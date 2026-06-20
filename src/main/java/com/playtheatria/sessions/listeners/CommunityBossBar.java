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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 * Pops a transient, server-wide boss bar whenever the community sell-hand bonus is worth a glance.
 *
 * <p>The bonus itself is granted in {@link RewardCommunity}; this just surfaces it. The bar shows
 * the live boost (e.g. {@code +30% sell hand}) and nudges players to {@code /daily-reward} for the
 * full breakdown. Rather than hanging on screen all day, it behaves like a notification: it appears
 * for a configured number of seconds when a player joins and again whenever the bonus level
 * changes, then fades.
 *
 * <p>There is one shared {@link BossBar} (Adventure fans every text/fill update out to whichever
 * players are viewing it), but the fade timer is tracked <em>per player</em> in {@link #hideTasks}:
 * if one player joins ten seconds after another, hiding the bar on the first player's schedule must
 * not yank it from the second mid-window. Showing the bar to a player therefore cancels and
 * reschedules just that player's hide.
 *
 * <p>The active tier is always derived from the persisted reward count
 * ({@link RewardTier#getNearestTier(int)} over {@link DailyStatsService#getRewardsEarned()}), so a
 * mid-day restart re-announces the correct bonus on join without storing any extra state. The bar is
 * only ever shown when both {@code community-rewards-enabled} and {@code community-bonus.boss-bar}
 * are on, and any lingering bar is taken down at the daily reset when the bonus is revoked.
 */
public class CommunityBossBar implements Listener {
    // Mirrors the green/gold styling of the RewardCommunity "Boon" message so the two read alike.
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final String GRADIENT = "<gradient:#67e9a8:#1fa86b>";
    private static final String HL = "<color:#ffd119>";
    private static final String HL_END = "</color>";

    private final TheatriaSessions plugin;
    private final ConfigManager cm;
    private final DailyStatsService dailyStatsService;
    private final PLog log;

    // One shared bar reused for the whole server; we only ever swap its text and fill.
    private final BossBar bar =
            BossBar.bossBar(
                    Component.empty(), 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);

    // Pending fade-out task per viewing player. Main-thread only, so a plain map is safe.
    private final Map<UUID, BukkitTask> hideTasks = new HashMap<>();

    public CommunityBossBar(
            TheatriaSessions plugin,
            ConfigManager cm,
            DailyStatsService dailyStatsService,
            PLog log) {
        this.plugin = plugin;
        this.cm = cm;
        this.dailyStatsService = dailyStatsService;
        this.log = log;
    }

    /**
     * Re-announces (or clears) the bar for everyone currently online. Called on enable so a
     * {@code /reload} mid-bonus pops the bar once for players who are already on the server.
     */
    public void refresh() {
        RewardTier tier = activeTier();
        if (tier == null) {
            hideFromAll();
            return;
        }
        applyTier(tier);
        announceToAll();
    }

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        if (!isEnabled()) return;
        applyTier(event.getRewardTier());
        announceToAll();
        log.debugFmt("[CommunityBossBar] Announcing boss bar for %s", event.getRewardTier());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RewardTier tier = activeTier();
        if (tier == null) return;
        // A late joiner needs the current bar; refresh the text first in case they are the first
        // viewer since a restart.
        applyTier(tier);
        announceTo(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Adventure drops the disconnecting player as a viewer on its own; just cancel their pending
        // fade so the map does not leak.
        BukkitTask task = hideTasks.remove(event.getPlayer().getUniqueId());
        if (task != null) task.cancel();
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        // The bonus is revoked at the reset, so any lingering bar must always come down here -- we
        // cannot derive this from the reward count because that may not be zeroed yet at this priority.
        hideFromAll();
        log.debug("[CommunityBossBar] Cleared community boss bar at daily reset.");
    }

    /** Removes the bar from every online player and cancels all fades (daily reset, plugin disable). */
    public void hideFromAll() {
        for (BukkitTask task : hideTasks.values()) {
            task.cancel();
        }
        hideTasks.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.hideBossBar(bar);
        }
    }

    private void announceToAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            announceTo(player);
        }
    }

    /** Shows the bar to one player and (re)starts their fade timer. */
    private void announceTo(Player player) {
        UUID id = player.getUniqueId();
        BukkitTask existing = hideTasks.remove(id);
        if (existing != null) existing.cancel();

        player.showBossBar(bar);

        long ticks = cm.getCommunityBossBarSeconds() * 20L;
        BukkitTask task =
                Bukkit.getScheduler()
                        .runTaskLater(
                                plugin,
                                () -> {
                                    hideTasks.remove(id);
                                    Player viewer = Bukkit.getPlayer(id);
                                    if (viewer != null) viewer.hideBossBar(bar);
                                },
                                ticks);
        hideTasks.put(id, task);
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
