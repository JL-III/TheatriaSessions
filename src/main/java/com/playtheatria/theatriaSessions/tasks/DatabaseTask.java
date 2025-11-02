package com.playtheatria.theatriaSessions.tasks;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.errors.PersistenceException;
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
            CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.sessionRepository = sessionRepository;
        this.sessionManager = sessionManager;
        this.serverSessionRepository = serverSessionRepository;
        this.serverSessionManager = serverSessionManager;
        this.customLogger = customLogger;
    }

    @Override
    public void run() {
        customLogger.sendFormattedLog(
                "DatabaseTask: " + sessionManager.getSessions().size() + " sessions found.");
        for (Session session : sessionManager.getSessions().values()) {
            switch (sessionRepository.createOrUpdate(session)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> customLogger
                        .sendDebug(String.format("Session persisted successfully: %s", ok.value()));
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> {
                    customLogger.sendFormattedLog(
                            String.format("Error persisting session %s", session.getPlayerName()));
                    customLogger.sendFormattedLog(
                            String.format("SessionTime: %s", session.getSessionTime()));
                    customLogger.sendFormattedLog(
                            String.format("Error: %s", err.error().getMessage()));
                }
            }
        }

        switch (serverSessionRepository.createOrUpdate(serverSessionManager.getServerSession())) {
            case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> customLogger.sendDebug(
                    String.format("ServerSession persisted successfully: %s", ok.value()));
            case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> customLogger.sendDebug(
                    String.format("Error persisting ServerSession: %s", err.error().getMessage()));
        }
    }
}
