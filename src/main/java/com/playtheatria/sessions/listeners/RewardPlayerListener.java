package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.events.IncrementRewardCountEvent;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.utils.PLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardPlayerListener implements Listener {
    private final ConfigManager configManager;
    private final PLog log;

    public RewardPlayerListener(ConfigManager configManager, PLog log) {
        this.configManager = configManager;
        this.log = log;
    }

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getSession().getPlayerUUID());
        if (player == null || !player.isOnline()) {
            log.info(
                    "Tried to reward a player but the player in the session returned as offline or"
                            + " null.");
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage(
                Component.text(
                                "Great work! You hit today’s daily-reward goal! Thanks for making"
                                        + " Theatria awesome!")
                        .color(NamedTextColor.GOLD));
        for (String reward : configManager.getRewards()) {
            String parsedCommand =
                    reward.replace("{player}", player.getName()) // Replace player name
                            .replace(
                                    "{player_uuid}",
                                    player.getUniqueId().toString()) // Replace UUID
                            .replace("{world}", player.getWorld().getName()); // Replace world
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            log.info("Sent reward of: " + parsedCommand + " to " + player.getName());
        }
        Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
    }
}
