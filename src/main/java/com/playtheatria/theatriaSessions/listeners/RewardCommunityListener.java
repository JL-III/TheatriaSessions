package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.events.RewardCommunityEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardCommunityListener implements Listener {
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;
    private final ConfigManager configManager;

    public RewardCommunityListener(
            CustomLogger<TheatriaSessions, ConfigManager> customLogger,
            ConfigManager configManager) {
        this.customLogger = customLogger;
        this.configManager = configManager;
    }

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        if (configManager.isCommunityRewardsEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(event.getRewardTier().getPermission())) continue;
                player.sendMessage(
                        customLogger.formatMessage(
                                "Alert!", "The community received a community reward!"));
                player.sendMessage(
                        Component.text("Use /daily-reward for more info!")
                                .color(TextColor.fromHexString(Util.COLOR_THREE)));
            }
            Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "lp group default permission settemp "
                            + event.getRewardTier().getPermission()
                            + " true 1day");
        } else {
            customLogger.sendDebug("Reward Community Event fired!");
        }
    }
}
