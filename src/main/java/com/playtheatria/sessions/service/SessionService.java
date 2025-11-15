package com.playtheatria.sessions.service;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.cache.SessionCache;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.SessionRepo;
import com.playtheatria.sessions.errors.PersistenceException;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SessionService {
    private final SessionCache cache;
    private final SessionRepo repo;
    private static final Logger logger = Logger.getLogger(SessionService.class.getName());

    public SessionService(SessionCache cache, SessionRepo repo) {
        this.cache = cache;
        this.repo = repo;
    }

    public void reset() {
        cache.resetSessions();
        switch (repo.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> logger.log(Level.INFO,
                    String.format("Deleted %d" + " entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> logger.log(Level.SEVERE,
                    String.format("Purging SessionRepository failed %s", err.error().getMessage()));
        }
    }

    public Result<Session, Exception> getSession(UUID playerUUID) {
        return cache.getSession(playerUUID);
    }

    public boolean hasSession(UUID playerUUID) {
        return cache.hasSession(playerUUID);
    }

    public void createNewSession(UUID playerUUID, String playerName) {
        cache.createNewSession(playerUUID, playerName);
    }

    public Collection<Session> getSessions() {
        return cache.getSessions().values();
    }

    public int getSessionsCount() {
        return cache.getSessions().size();
    }

    public void addSession(Session session) {
        cache.addSession(session);
    }

    public void persist(boolean verbose) {
        for (Session session : getSessions()) {
            switch (repo.createOrUpdate(session)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                    Dao.CreateOrUpdateStatus status = ok.value();
                    String msg =
                            String.format(
                                    "Session persisted successfully | created: %s, updated: %s,"
                                            + " lines updated: %s",
                                    status.isCreated(),
                                    status.isUpdated(),
                                    status.getNumLinesChanged());
                    if (verbose) {
                        logger.log(Level.INFO, "{0}{1}", new Object[]{msg, session});
                    } else {
                        logger.log(Level.INFO, "{0}{1}", new Object[]{msg, session});
                    }
                }
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> {
                    String msg =
                            String.format("Error persisting session: %s", err.error().getMessage());
                    logger.log(Level.SEVERE, "{0}{1}", new Object[]{msg, session});
                }
            }
        }
    }
}
