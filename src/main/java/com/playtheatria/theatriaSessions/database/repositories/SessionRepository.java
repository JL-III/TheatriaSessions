package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.data.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionRepository {
    private final Dao<Session, String> dao;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public SessionRepository(
            TheatriaSessionsDB theatriaSessionsDB,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger
    ) throws SQLException {
        dao = theatriaSessionsDB.getDao(Session.class);
        this.customLogger = customLogger;
    }

    // Load all sessions from the database
    public List<Session> loadSessions() {
        List<Session> sessions;
        try {
            // Query all rows from the database
            sessions = dao.queryForAll();
            customLogger.sendFormattedLog("Loaded " + sessions.size() + " sessions from the database.");
            return sessions;
        } catch (SQLException e) {
            customLogger.sendFormattedLog("Failed to load sessions from the database: " + e.getMessage());
            e.printStackTrace();
            customLogger.sendFormattedLog("Returning and empty list of sessions.");
        }
        return new ArrayList<>();
    }

    /**
     * Creates or updates a session
     * @param session the session we are going to persist in the database, this is used for persisting sessions between server resets.
     * @return returns true if creation or update was successful, returns false if something failed.
     */
    public boolean createOrUpdate(Session session) {
        customLogger.sendDebug("Sending session to database for persistence. " + session.getSessionTime() + " " + session.getPlayerName() + " " + session.getPlayerUUID());
        try {
            dao.createOrUpdate(session);
            return true;
        } catch (SQLException exception) {
            customLogger.sendFormattedLog("Error on createOrUpdate Session: " + session.getSessionTime() + " " + session.getPlayerName() + " " + session.getPlayerUUID());
            return false;
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
