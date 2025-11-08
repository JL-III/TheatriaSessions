package com.playtheatria.sessions.tasks;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.ServerSessionRepository;
import com.playtheatria.sessions.database.repositories.SessionRepository;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.managers.ServerSessionManager;
import com.playtheatria.sessions.managers.SessionManager;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionRepository sessionRepository;
    private final ServerSessionRepository serverSessionRepository;
    private final SessionManager sessionManager;
    private final ServerSessionManager serverSessionManager;
    private final PLog log;

    public DatabaseTask(
            SessionRepository sessionRepository,
            ServerSessionRepository serverSessionRepository,
            SessionManager sessionManager,
            ServerSessionManager serverSessionManager,
            PLog log) {
        this.sessionRepository = sessionRepository;
        this.sessionManager = sessionManager;
        this.serverSessionRepository = serverSessionRepository;
        this.serverSessionManager = serverSessionManager;
        this.log = log;
    }

    @Override
    public void run() {
        log.debug("DatabaseTask: " + sessionManager.getSessions().size() + " sessions.");
        for (Session session : sessionManager.getSessions().values()) {
            switch (sessionRepository.createOrUpdate(session)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                    log.debug(
                            Util.summary(
                                    String.format("Session persisted successfully: %s", ok.value()),
                                    session));
                }
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> {
                    log.debug(
                            Util.summary(
                                    String.format(
                                            "Error persisting session: %s",
                                            err.error().getMessage()),
                                    session));
                }
            }
        }

        switch (serverSessionRepository.createOrUpdate(serverSessionManager.getServerSession())) {
            case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> log.debug(
                    String.format("ServerSession persisted successfully: %s", ok.value()));
            case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> log.info(
                    String.format("Error persisting ServerSession: %s", err.error().getMessage()));
        }
    }
}
