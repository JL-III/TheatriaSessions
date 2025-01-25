package com.playtheatria.theatriaSessions.tasks;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionRepository sessionRepository;
    private final ServerSessionRepository serverSessionRepository;
    private final SessionManager sessionManager;
    private final ServerSessionManager serverSessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public DatabaseTask(
            SessionRepository sessionRepository,
            ServerSessionRepository serverSessionRepository,
            SessionManager sessionManager,
            ServerSessionManager serverSessionManager,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionManager = sessionManager;
        this.serverSessionRepository = serverSessionRepository;
        this.serverSessionManager = serverSessionManager;
        this.customLogger = customLogger;
    }

    @Override
    public void run() {
        customLogger.sendFormattedLog("Persisting data now. " + sessionManager.getSessions().size() + " sessions found." );
        for (Session session : sessionManager.getSessions().values()) {
            if (!sessionRepository.createOrUpdate(session)) {
                customLogger.sendFormattedLog("Error persisting session to database " + session.getPlayerName() + session.getSessionTime());
            }
        }
        serverSessionRepository.createOrUpdate(serverSessionManager.getServerSession());
    }
}
