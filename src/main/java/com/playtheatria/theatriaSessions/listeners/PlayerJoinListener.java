package com.playtheatria.theatriaSessions.listeners;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;

public class PlayerJoinListener implements Listener {
    private final SessionManager sessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public PlayerJoinListener(
            SessionManager sessionManager,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.sessionManager = sessionManager;
        this.customLogger = customLogger;
    }

    /**
     * Handles PlayerJoinEvent to create a new session if one does not exist
     * @param event PlayerJoinEvent instance
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if player has permission to have a session
        if (!event.getPlayer().hasPermission("theatria.sessions.allow")) return;

        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        if (!sessionManager.hasSession(playerUUID)) {
            customLogger.sendFormattedLog("No session found for " + playerName);
            sessionManager.createNewSession(playerUUID, playerName);
        }
    }
}
