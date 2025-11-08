package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.events.RewardCommunityEvent;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardCommunityListener implements Listener {
    private final ConfigManager configManager;
    private final PLog log;

    public RewardCommunityListener(ConfigManager configManager, PLog log) {
        this.log = log;
        this.configManager = configManager;
    }

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        if (configManager.isCommunityRewardsEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(event.getRewardTier().getPermission())) continue;
                player.sendMessage(
                        Util.formatMessage("Alert!", "The community received a community reward!"));
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
            log.debug("Reward Community Event fired!");
        }
    }
}
