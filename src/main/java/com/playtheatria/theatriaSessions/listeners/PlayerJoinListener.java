package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final SessionManager sessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public PlayerJoinListener(SessionManager sessionManager, CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.sessionManager = sessionManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!sessionManager.hasSession(event.getPlayer().getUniqueId()) && event.getPlayer().hasPermission("theatria.sessions.allow")) {
            customLogger.sendFormattedLog("No session found for " + event.getPlayer().getName() + " creating one now.");
            sessionManager.createNewSession(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    }
}
