package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.managers.SessionCache;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final SessionCache sessionCache;
    private final PLog log;

    public PlayerJoin(SessionCache sessionCache, PLog log) {
        this.sessionCache = sessionCache;
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

        if (!sessionCache.hasSession(playerUUID)) {
            log.info("No session found for " + playerName);
            sessionCache.createNewSession(playerUUID, playerName);
        }
    }
}
