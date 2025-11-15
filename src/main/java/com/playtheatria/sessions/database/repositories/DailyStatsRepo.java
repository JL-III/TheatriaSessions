package com.playtheatria.sessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.errors.PersistenceException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DailyStatsRepo {
    private final Dao<DailyStats, String> dao;
    private static final Logger logger = Logger.getLogger(DailyStatsRepo.class.getName());

    public DailyStatsRepo(TheatriaSessionsDB sessionsDB) throws SQLException {
        this.dao = sessionsDB.getDao(DailyStats.class);
    }

    /**
     * This method checks for a DailyStats inside the database that matches today's date.
     * If one isn't found a new one is created and stored in the database.
     * If one isn't able to save, we still return a new DailyStats for the plugin to continue.
     * The plugin shuts down if there is an SQLException
     * @return DailyStats for the ServerRepository, returns the database DailyStats or returns a new one if one couldn't be loaded.
     * @throws IllegalStateException if the plugin can't save, propagates the SQLException
     */
    public Result<DailyStats, Exception> load() {
        try {
            DailyStats dailyStats = dao.queryForId("0");
            if (dailyStats == null) {
                logger.log(Level.INFO, "No DailyStats found in database with the id of 0. Creating a new entry.");
                dailyStats = new DailyStats(LocalDate.now(TimeUtils.timeZone));
                dao.create(dailyStats);
            } else {
                logger.log(Level.INFO, "Loaded DailyStats from the database.");
            }
            return new Ok<>(dailyStats);
        } catch (SQLException exception) {
            logger.log(Level.INFO, "Failed to load DailyStats from the database: {0}", exception.getMessage());
            return new Err<>(
                    new IllegalStateException("SQLException while managing DailyStats", exception));
        }
    }

    /**
     * Creates or updates DailyStats
     * @param dailyStats the DailyStats we are going to persist in the database, this is used for persisting sessions between server resets.
     * @return Result containing Dao.CreateOrUpdateStatus if successful, or a PersistenceException if something failed.
     */
    public Result<Dao.CreateOrUpdateStatus, PersistenceException> createOrUpdate(
            DailyStats dailyStats) {
        logger.log(Level.INFO, "createOrUpdate called on a dailyStats");
        logger.log(Level.INFO, String.format("Date %s", dailyStats.getDate()));
        logger.log(Level.INFO, String.format("PlayersJoined: %s", dailyStats.getPlayersJoined()));
        logger.log(Level.INFO, String.format("RewardsEarned: %s", dailyStats.getRewardsEarned()));
        try {
            return new Ok<>(dao.createOrUpdate(dailyStats));
        } catch (SQLException exception) {
            return new Err<>(
                    new PersistenceException(
                            "Err persisting daily_stats " + dailyStats.getDate(), exception));
        }
    }

    /**
     * Purges all entries from the DailyStats table
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
