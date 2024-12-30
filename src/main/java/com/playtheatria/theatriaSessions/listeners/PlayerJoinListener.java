package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.tasks.SessionTask;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final SessionTask sessionTask;

    public PlayerJoin(SessionTask sessionTask) {
        this.sessionTask = sessionTask;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!sessionTask.hasSession(event.getPlayer().getUniqueId())) {
            Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("No session found for " + event.getPlayer().getName() + " creating one now.")));
            sessionTask.addSession(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    }
}
