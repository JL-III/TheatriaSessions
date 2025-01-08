package com.playtheatria.theatriaSessions.tasks;

import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionRepository sessionRepository;
    private final ServerSessionRepository serverSessionRepository;
    private final SessionManager sessionManager;
    private final ServerSessionManager serverSessionManager;

    public DatabaseTask(SessionRepository sessionRepository, ServerSessionRepository serverSessionRepository, SessionManager sessionManager, ServerSessionManager serverSessionManager) {
        this.sessionRepository = sessionRepository;
        this.serverSessionRepository = serverSessionRepository;
        this.sessionManager = sessionManager;
        this.serverSessionManager = serverSessionManager;
    }

    @Override
    public void run() {
        Util.sendFormattedLog("Persisting current sessions now. " + sessionManager.getSessions().size() + " sessions found." );
        for (Session session : sessionManager.getSessions()) {
            if (!sessionRepository.createOrUpdate(session)) {
                Util.sendFormattedLog("Error persisting session to database " + session.getPlayerName() + session.getSessionTime());
            }
        }
        Util.sendFormattedLog("Persisting current ServerSession now. " + serverSessionManager.getServerSession().getSessionDate() + " rewards earned: " + serverSessionManager.getServerSession().getRewardsEarned() + " joins: "  + serverSessionManager.getServerSession().getPlayersJoined());
        serverSessionRepository.createOrUpdate(serverSessionManager.getServerSession());
    }
}
