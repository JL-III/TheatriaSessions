package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.theatriaSessions.data.ServerSession;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;

import java.sql.SQLException;
import java.time.LocalDate;

public class ServerSessionRepository {
    private final Dao<ServerSession, String> dao;
    private final CustomLogger customLogger;

    public ServerSessionRepository(TheatriaSessionsDB theatriaSessionsDB, CustomLogger customLogger) throws SQLException {
        this.dao = theatriaSessionsDB.getDao(ServerSession.class);
        this.customLogger = customLogger;
    }

    public ServerSession loadServerSession() {
        try {
            ServerSession serverSession = dao.queryForId(LocalDate.now().toString());
            if (serverSession == null) {
                Util.sendFormattedLog("No ServerSession found for today. Creating a new ServerSession.");
                serverSession = new ServerSession(LocalDate.now());
                dao.create(serverSession); // Save the new session to the database
            } else {
                Util.sendFormattedLog("Loaded ServerSession from the database.");
            }
            return serverSession;
        } catch (SQLException exception) {
            Util.sendFormattedLog("Failed to load ServerSession from the database: " + exception.getMessage());
            exception.printStackTrace();
            Util.sendFormattedLog("Creating new ServerSession.");
        }
        return new ServerSession(LocalDate.now());
    }


    public boolean createOrUpdate(ServerSession serverSession) {
        customLogger.sendDebug("Create or update");
        try {
            dao.createOrUpdate(serverSession);
            return true;
        } catch (SQLException exception) {
            Util.sendFormattedLog("Error on createOrUpdate ServerSession: " + serverSession.getSessionDate() + " rewardsEarned: " + serverSession.getRewardsEarned() + "| playersJoined: " + serverSession.getPlayersJoined());
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
