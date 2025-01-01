package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            Util.sendFormattedLog("Tried to reward a player but the player in the session returned as offline or null.");
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage(Component.text("You achieved the session requirement for today!").color(NamedTextColor.GOLD));
        for (String reward : configManager.getRewards()) {
            String parsedCommand = reward
                    .replace("{player}", player.getName()) // Replace player name
                    .replace("{player_uuid}", player.getUniqueId().toString()) // Replace UUID
                    .replace("{world}", player.getWorld().getName()); // Replace world
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            Util.sendFormattedLog("Sent reward of: " + reward + " to " + player.getName());
        }
    }
}
