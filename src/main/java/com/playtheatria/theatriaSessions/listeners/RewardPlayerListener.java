package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.events.IncrementRewardCountEvent;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardPlayerListener implements Listener {
    private final ConfigManager configManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public RewardPlayerListener(ConfigManager configManager, CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.configManager = configManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getSession().getPlayerUUID());
        if (player == null || !player.isOnline()) {
            customLogger.sendFormattedLog("Tried to reward a player but the player in the session returned as offline or null.");
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage(Component.text("Great work! You hit today’s daily-reward goal! Thanks for making Theatria awesome!").color(NamedTextColor.GOLD));
        for (String reward : configManager.getRewards()) {
            String parsedCommand = reward
                    .replace("{player}", player.getName()) // Replace player name
                    .replace("{player_uuid}", player.getUniqueId().toString()) // Replace UUID
                    .replace("{world}", player.getWorld().getName()); // Replace world
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            customLogger.sendFormattedLog("Sent reward of: " + parsedCommand + " to " + player.getName());
        }
        Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
    }
}
