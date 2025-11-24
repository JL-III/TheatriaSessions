package com.playtheatria.sessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.errors.RepositoryException;
import com.playtheatria.sessions.utils.PLog;
import java.sql.SQLException;
import java.util.List;

public class SessionRepo {
    private final Dao<Session, String> dao;
    private final PLog log;

    public SessionRepo(TheatriaSessionsDB sessionsDB, PLog log) throws SQLException {
        dao = sessionsDB.getDao(Session.class);
        this.log = log;
    }

    // TODO
    // Check behavior, does this load an empty list if no sessions exist?
    public Result<List<Session>, RepositoryException> load() {
        try {
            List<Session> items = dao.queryForAll();
            log.debugFmt("Loaded %s sessions from the database.", items.size());
            return new Ok<>(items);
        } catch (SQLException e) {
            return new Err<>(
                    new RepositoryException("Failed to load sessions from the database", e));
        }
    }

    /**
     * Creates or updates a Session in the database
     * @param session The Session to create or update
     * @return Result containing the CreateOrUpdateStatus if successful, or a PersistenceException if something failed.
     */
    public Result<Dao.CreateOrUpdateStatus, PersistenceException> createOrUpdate(Session session) {
        log.debugFmt("Persisting Session %s", session);
        log.debugFmt("[createOrUpdate] Running on thread: %s", Thread.currentThread().getName());
        try {
            return new Ok<>(dao.createOrUpdate(session));
        } catch (SQLException exception) {
            log.errFmt("CreateOrUpdate failed %s", session);
            return new Err<>(
                    new PersistenceException("Failed to create or update Session", exception));
        }
    }

    /**
     * Purges all entries from the Session table
     * @return Result containing the number of deleted entries if successful, or a PersistenceException if something failed.
     */
    public Result<Integer, PersistenceException> purgeAll() {
        try {
            return new Ok<>(dao.delete(dao.queryForAll()));
        } catch (SQLException e) {
            return new Err<>(
                    new PersistenceException("Failed to purge all entries from Session table", e));
        }
    }
}
