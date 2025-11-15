package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final SessionService sessionService;
    private final PLog log;

    public PlayerJoin(SessionService sessionService, PLog log) {
        this.sessionService = sessionService;
        this.log = log;
    }

    /**
     * Handles PlayerJoinEvent to create a new session if one does not exist
     * @param event PlayerJoinEvent instance
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if player has permission to have a session
        if (!event.getPlayer().hasPermission(Util.PERMISSION_ALLOW)) return;

        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        if (!sessionService.hasSession(playerUUID)) {
            log.info("No session found for " + playerName);
            sessionService.createNewSession(playerUUID, playerName);
        }
    }
}
