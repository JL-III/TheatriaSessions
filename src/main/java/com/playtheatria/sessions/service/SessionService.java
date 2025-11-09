package com.playtheatria.sessions.service;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.cache.SessionCache;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.SessionRepo;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import java.util.Collection;
import java.util.UUID;

public class SessionService {
    private final SessionCache cache;
    private final SessionRepo repo;
    private final PLog log;

    public SessionService(SessionCache cache, SessionRepo repo, PLog log) {
        this.cache = cache;
        this.repo = repo;
        this.log = log;
    }

    public void reset() {
        cache.resetSessions();
        switch (repo.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> log.debug(
                    String.format("Deleted %d" + " entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> log.err(
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

    public void persist() {
        for (Session session : cache.getSessions().values()) {
            switch (repo.createOrUpdate(session)) {
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
    }
}
