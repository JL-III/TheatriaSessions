package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.utils.Util;

import java.sql.SQLException;

public class SessionRepository {
    private final Dao<Session, String> dao;

    public SessionRepository(TheatriaSessionsDB theatriaSessionsDB) throws SQLException {
        dao = theatriaSessionsDB.getDao(Session.class);
    }

    /**
     * Creates or updates a session
     * @param session the session we are going to persist in the database, this is used for persisting sessions between server resets.
     * @return returns true if creation or update was successful, returns false if something failed.
     */
    public boolean createOrUpdate(Session session) {
        Util.sendFormattedLog("Sending session to database for persistence. " + session.getSessionTime() + " " + session.getPlayerName() + " " + session.getPlayerUUID());
        try {
            dao.createOrUpdate(session);
            return true;
        } catch (SQLException exception) {
            Util.sendFormattedLog("Error on createOrUpdate Session: " + session.getSessionTime() + " " + session.getPlayerName() + " " + session.getPlayerUUID());
            return false;
        }
    }

    public boolean purgeAll() {
        try {
            dao.delete(dao.queryForAll());
            return true;
        } catch (SQLException e) {
            Util.sendFormattedLog("Failed to purge all entries" + e.getMessage());
            return false;
        }
    }
}
