package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.enums.RewardTier;
import com.playtheatria.sessions.events.RewardCommunityEvent;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
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
        if (configManager.isCommunityRewardsEnabled()) {
            RewardTier tier = event.getRewardTier();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(tier.getPermission())) continue;
                player.sendMessage(
                        Util.formatMessage(
                                "Community",
                                "Unlocked "
                                        + tier.getDisplayName()
                                        + " (+"
                                        + tier.getPercentage()
                                        + " sell hand) until reset!"));
                player.sendMessage(
                        Component.text("Use /community for details.")
                                .color(TextColor.fromHexString(Util.COLOR_THREE)));
            }
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(), Util.grantCommunityPermCommand(tier.getPermission()));
        } else {
            log.debug("Reward Community Event fired!");
        }
    }
}
