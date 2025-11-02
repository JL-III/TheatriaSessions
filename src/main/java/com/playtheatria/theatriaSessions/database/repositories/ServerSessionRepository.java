package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.data.ServerSession;
import com.playtheatria.theatriaSessions.errors.PersistenceException;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import org.bukkit.Bukkit;

public class ServerSessionRepository {
    private final Dao<ServerSession, String> dao;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public ServerSessionRepository(
            TheatriaSessionsDB theatriaSessionsDB,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger)
            throws SQLException {
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
            ServerSession serverSession = dao.queryForId("0");
            if (serverSession == null) {
                customLogger.sendFormattedLog(
                        "No ServerSession found in database with the id of 0. Creating a new"
                                + " ServerSession.");
                serverSession = new ServerSession(LocalDate.now(TimeUtils.timeZone));
                dao.create(serverSession); // Save the new session to the database
                // Warning - listener doesn't exist yet
                Bukkit.getPluginManager().callEvent(new DayChangeEvent());
            } else {
                customLogger.sendFormattedLog("Loaded ServerSession from the database.");
            }
            return serverSession;
        } catch (SQLException exception) {
            customLogger.sendFormattedLog(
                    "Failed to load ServerSession from the database: " + exception.getMessage());
            exception.printStackTrace();
            customLogger.sendFormattedLog("Creating new ServerSession.");
            Bukkit.getPluginManager()
                    .disablePlugin(
                            Objects.requireNonNull(
                                    Bukkit.getPluginManager().getPlugin("TheatriaSessions")));
            throw new IllegalStateException("SQLException while managing ServerSession", exception);
        }
    }

    /**
     * Creates or updates a ServerSession
     * @param serverSess the ServerSession we are going to persist in the database, this is used for persisting sessions between server resets.
     * @return Result containing Dao.CreateOrUpdateStatus if successful, or a PersistenceException if something failed.
     */
    public Result<Dao.CreateOrUpdateStatus, PersistenceException> createOrUpdate(
            ServerSession serverSess) {
        customLogger.sendDebug("createOrUpdate called on a ServerSession");
        customLogger.sendDebug(String.format("SessionDate %s", serverSess.getSessionDate()));
        customLogger.sendDebug(String.format("PlayersJoined: %s", serverSess.getPlayersJoined()));
        customLogger.sendDebug(String.format("RewardsEarned: %s", serverSess.getRewardsEarned()));
        try {
            return new Ok<>(dao.createOrUpdate(serverSess));
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Err persisting ServerSession " + serverSess.getSessionDate(),
                            exception));
        }
    }

    /**
     * Purges all entries from the ServerSession table
     * @return Result containing the number of deleted entries if successful, or a PersistenceException if something failed.
     */
    public Result<Integer, PersistenceException> purgeAll() {
        try {
            return new Ok<>(dao.delete(dao.queryForAll()));
        } catch (SQLException e) {
            return new Err<>(
                    new PersistenceException(
                            "Failed to purge all entries from ServerSession table", e));
        }
    }
}
