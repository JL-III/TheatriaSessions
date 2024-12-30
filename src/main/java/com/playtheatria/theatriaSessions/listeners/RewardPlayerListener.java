package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardPlayerListener implements Listener {

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getSession().getPlayerUUID());
        if (player == null || !player.isOnline()) {
            Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("Tried to reward a player but the player in the session returned as offline or null.")));
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage(Component.text("You achieved the session requirement for today!").color(NamedTextColor.GOLD));
        player.sendMessage(Util.formatMessage("You have been awarded session keys! Use them at ", "/warp crates"));
        Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("Reward given to player: " + player.getName())));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cc give p sessioncrate 5 " + player.getName());
    }
}
