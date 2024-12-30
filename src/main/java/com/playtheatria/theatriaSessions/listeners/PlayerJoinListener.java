package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final SessionManager sessionManager;

    public PlayerJoinListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!sessionManager.hasSession(event.getPlayer().getUniqueId())) {
            Util.sendFormattedLog("No session found for " + event.getPlayer().getName() + " creating one now.");
            sessionManager.addSession(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    }
}
