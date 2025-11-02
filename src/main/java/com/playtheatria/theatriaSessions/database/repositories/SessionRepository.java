package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.errors.PersistenceException;
import com.playtheatria.theatriaSessions.errors.RepositoryException;
import java.sql.SQLException;
import java.util.List;

public class SessionRepository {
    private final Dao<Session, String> dao;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public SessionRepository(
            TheatriaSessionsDB theatriaSessionsDB,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger)
            throws SQLException {
        dao = theatriaSessionsDB.getDao(Session.class);
        this.customLogger = customLogger;
    }

    // TODO
    // Check behavior, does this load an empty list if no sessions exist?
    public Result<List<Session>, RepositoryException> loadSessions() {
        try {
            return new Ok<>(dao.queryForAll());
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
        String sessionSummary =
                "time="
                        + session.getSessionTime()
                        + " name="
                        + session.getPlayerName()
                        + " uuid="
                        + session.getPlayerUUID();

        customLogger.sendDebug("Session Info | " + sessionSummary);
        try {
            return new Ok<>(dao.createOrUpdate(session));
        } catch (SQLException exception) {
            customLogger.sendFormattedLog("CreateOrUpdate failed | " + sessionSummary);
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
