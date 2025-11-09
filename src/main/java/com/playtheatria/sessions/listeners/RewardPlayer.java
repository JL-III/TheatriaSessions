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

public class RewardPlayer implements Listener {
    private final ConfigManager cm;
    private final PLog log;

    public RewardPlayer(ConfigManager configManager, PLog log) {
        this.cm = configManager;
        this.log = log;
    }

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getSession().getPlayerUUID());
        if (player == null || !player.isOnline()) {
            log.warn("Player reward returned offline or null");
            log.warn(String.format("Player UUID: %s", event.getSession().getPlayerUUID()));
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage(Component.text(cm.getRewardMessage()).color(NamedTextColor.GOLD));

        for (String rewardString : cm.getRewards()) {
            String parsedCommand = parseCommand(player, rewardString);

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
            log.info("Sent reward of: " + parsedCommand + " to " + player.getName());
        }
        Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
    }

    private static String parseCommand(Player player, String rewardString) {
        return rewardString
                .replace("{player}", player.getName())
                .replace("{player_uuid}", player.getUniqueId().toString())
                .replace("{world}", player.getWorld().getName());
    }
}
