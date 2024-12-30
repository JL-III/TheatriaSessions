package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class RewardPlayerListener implements Listener {

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getSession().getPlayerUUID());
        if (player == null || !player.isOnline()) {
            Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("Tried to reward a player but the player in the session returned as offline or null.")));
            return;
        }

        event.getSession().setRewarded();
        player.sendMessage("You achieved the session requirement for today!");
        Bukkit.getConsoleSender().sendMessage(
                "Reward given to player: " + player.getName()
                        + "ItemsDropped: " + player.getInventory().addItem(new ItemStack(Material.NETHER_STAR, 10))
        );
    }
}
