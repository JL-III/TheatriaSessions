package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.theatriaSessions.data.ServerSession;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class ServerSessionRepository {
    private final Dao<ServerSession, String> dao;
    private final CustomLogger customLogger;

    public ServerSessionRepository(TheatriaSessionsDB theatriaSessionsDB, CustomLogger customLogger) throws SQLException {
        this.dao = theatriaSessionsDB.getDao(ServerSession.class);
        this.customLogger = customLogger;
    }

    /**
     * This method checks for a ServerSession inside the database that matches today's date.
     * If one isn't found a new one is created and stored in the database.
     * If one isn't able to save, we still return a new ServerSession for the plugin to continue.
     * The plugin shuts down if there is an SQLException
     * @return ServerSession for the ServerRepository, returns the database ServerSession or returns a new one if one couldn't be loaded.
     * @throws IllegalStateException if the plugin can't save, propagates the SQLException
     */
    public ServerSession loadServerSession() {
        try {
            ServerSession serverSession = dao.queryForId(LocalDate.now().toString());
            if (serverSession == null) {
                Util.sendFormattedLog(String.format("No ServerSession found in database for today %s. Creating a new ServerSession.", LocalDate.now()));
                serverSession = new ServerSession(LocalDate.now());
                dao.create(serverSession); // Save the new session to the database
                // Warning - listener doesn't exist yet
                Bukkit.getPluginManager().callEvent(new DayChangeEvent());
            } else {
                Util.sendFormattedLog("Loaded ServerSession from the database.");
            }
            return serverSession;
        } catch (SQLException exception) {
            Util.sendFormattedLog("Failed to load ServerSession from the database: " + exception.getMessage());
            exception.printStackTrace();
            Util.sendFormattedLog("Creating new ServerSession.");
            Bukkit.getPluginManager().disablePlugin(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("TheatriaSessions")));
            throw new IllegalStateException("SQLException while managing ServerSession", exception);
        }
    }


    public boolean createOrUpdate(ServerSession serverSession) {
        customLogger.sendDebug(String.format("Create or update called on a ServerSession: %s", serverSession.getSessionDate()));
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
