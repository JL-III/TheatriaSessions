package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final ServerSessionManager serverSessionManager;
    private final SessionManager sessionManager;

    public PlayerJoinListener(ServerSessionManager serverSessionManager, SessionManager sessionManager) {
        this.serverSessionManager = serverSessionManager;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!sessionManager.hasSession(event.getPlayer().getUniqueId()) && event.getPlayer().hasPermission("theatria.sessions.allow")) {
            Util.sendFormattedLog("No session found for " + event.getPlayer().getName() + " creating one now.");
            sessionManager.addSession(event.getPlayer().getUniqueId(), event.getPlayer().getName());
            serverSessionManager.getServerSession().incrementPlayersJoined();
        }
    }
}
