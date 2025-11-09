package com.playtheatria.sessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import org.bukkit.Bukkit;

public class DailyStatsRepo {
    private final Dao<DailyStats, String> dao;
    private final PLog log;

    public DailyStatsRepo(TheatriaSessionsDB sessionsDB, PLog log) throws SQLException {
        this.dao = sessionsDB.getDao(DailyStats.class);
        this.log = log;
    }

    /**
     * This method checks for a ServerSession inside the database that matches today's date.
     * If one isn't found a new one is created and stored in the database.
     * If one isn't able to save, we still return a new ServerSession for the plugin to continue.
     * The plugin shuts down if there is an SQLException
     * @return ServerSession for the ServerRepository, returns the database ServerSession or returns a new one if one couldn't be loaded.
     * @throws IllegalStateException if the plugin can't save, propagates the SQLException
     */
    public DailyStats load() {
        try {
            DailyStats dailyStats = dao.queryForId("0");
            if (dailyStats == null) {
                log.info("No DailyStats found in database with the id of 0. Creating a new entry.");
                dailyStats = new DailyStats(LocalDate.now(TimeUtils.timeZone));
                dao.create(dailyStats); // Save the new session to the database
                // Warning - listener doesn't exist yet
                Bukkit.getPluginManager().callEvent(new DayChangeEvent());
            } else {
                log.info("Loaded DailyStats from the database.");
            }
            return dailyStats;
        } catch (SQLException exception) {
            log.info("Failed to load DailyStats from the database: " + exception.getMessage());
            log.info("Creating new DailyStats.");
            Bukkit.getPluginManager()
                    .disablePlugin(
                            Objects.requireNonNull(
                                    Bukkit.getPluginManager().getPlugin("TheatriaSessions")));
            throw new IllegalStateException("SQLException while managing DailyStats", exception);
        }
    }

    /**
     * Creates or updates a ServerSession
     * @param dailyStats the ServerSession we are going to persist in the database, this is used for persisting sessions between server resets.
     * @return Result containing Dao.CreateOrUpdateStatus if successful, or a PersistenceException if something failed.
     */
    public Result<Dao.CreateOrUpdateStatus, PersistenceException> createOrUpdate(
            DailyStats dailyStats) {
        log.debug("createOrUpdate called on a dailyStats");
        log.debug(String.format("Date %s", dailyStats.getDate()));
        log.debug(String.format("PlayersJoined: %s", dailyStats.getPlayersJoined()));
        log.debug(String.format("RewardsEarned: %s", dailyStats.getRewardsEarned()));
        try {
            return new Ok<>(dao.createOrUpdate(dailyStats));
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Err persisting daily_stats " + dailyStats.getDate(), exception));
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
                            "Failed to purge all entries from daily_stats table", e));
        }
    }
}
