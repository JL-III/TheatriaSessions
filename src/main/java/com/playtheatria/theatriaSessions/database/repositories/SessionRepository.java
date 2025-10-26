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

    /**
     * Load all sessions from the database
     * @return returns a Result containing the list of sessions if successful, or an Exception if something failed.
     */
    public Result<List<Session>, RepositoryException> loadSessions() {
        try {
            return new Ok<>(dao.queryForAll());
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Failed to load sessions from the database", exception));
        }
    }

    /**
     * Creates or updates a session
     * @param session the session to create or update
     * @return returns a Result containing true if successful, or an Exception if something failed.
     */
    public Result<Boolean, RepositoryException> createOrUpdate(Session session) {
        try {
            dao.createOrUpdate(session);
            return new Ok<>(true);
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Error on createOrUpdate Session: "
                                    + session.getSessionTime()
                                    + " "
                                    + session.getPlayerName()
                                    + " "
                                    + session.getPlayerUUID(),
                            exception));
        }
    }

    public boolean purgeAll() {
        try {
            dao.delete(dao.queryForAll());
            return true;
        } catch (SQLException e) {
            customLogger.sendFormattedLog("Failed to purge all entries" + e.getMessage());
            return false;
        }
    }
}
