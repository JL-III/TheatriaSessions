package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.RewardCommunityEvent;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardCommunity implements Listener {
    private final ConfigManager configManager;
    private final PLog log;

    public RewardCommunity(ConfigManager configManager, PLog log) {
        this.log = log;
        this.configManager = configManager;
    }

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        if (!configManager.isCommunityRewardsEnabled()) {
            log.debug("Reward Community Event fired!");
            return;
        }

        RewardTier tier = event.getRewardTier();

        // The unlock is surfaced via the community boss bar (see CommunityBossBar) -- we no longer
        // announce it in chat too, since that doubled up with the bar (and tripled up alongside the
        // streak messages). We only play a short chime here to mark the moment the bonus is unlocked.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(tier.getPermission())) continue;
            // A bright note-block chime to mark the unlock.
            player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 1.0f, 1.2f);
        }

        boolean granted =
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        Util.grantCommunityPermCommand(
                                tier.getPermission(),
                                configManager.getCommunityBonusGroup(),
                                configManager.getCommunityBonusDuration()));
        if (!granted) {
            log.warn(
                    "Failed to grant community permission '"
                            + tier.getPermission()
                            + "'. Is LuckPerms installed?");
        }
    }
}
