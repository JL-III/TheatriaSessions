package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.events.IncrementRewardCountEvent;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardPlayerListener implements Listener {
    private final ConfigManager configManager;

    public RewardPlayerListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getSession().getPlayerUUID());
        if (player == null || !player.isOnline()) {
            Util.sendFormattedLog("Tried to reward a player, but the player in the session (" + event.getSession().getPlayerUUID() + ") is offline or null.");
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage(Util.formatMessage("Alert!", "Great work! You hit your /daily-reward goal! Thanks for making Theatria awesome! Get other players to meet this goal unlock community rewards!"));
        for (String reward : configManager.getRewards()) {
            String parsedCommand = reward
                    .replace("{player}", player.getName()) // Replace player name
                    .replace("{player_uuid}", player.getUniqueId().toString()) // Replace UUID
                    .replace("{world}", player.getWorld().getName()); // Replace world
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            Util.sendFormattedLog("Sent reward of: " + parsedCommand + " to player: " + player.getName() + " (UUID: " + player.getUniqueId() + ")");
        }
        Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
    }
}
