package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.RewardCommunityEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardCommunityListener implements Listener {

    @EventHandler
    public void onRewardCommunity(RewardCommunityEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(event.getRewardTier().getPermission())) continue;
            player.sendMessage(Util.formatMessage("Alert!", "The community received a community reward!"));
            player.sendMessage(Component.text("Use /daily-reward for more info!").color(TextColor.fromHexString(Util.COLOR_THREE)));
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp group default permission settemp " + event.getRewardTier().getPermission() + " true 1day");
//        Util.sendFormattedLog("Sent community reward of: " + event.getRewardCommand() + " for reaching threshold of " + event.getThreshold());
    }
}
