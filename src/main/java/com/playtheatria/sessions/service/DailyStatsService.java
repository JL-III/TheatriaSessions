package com.playtheatria.sessions.service;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.cache.DailyStatsCache;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.database.repositories.DailyStatsRepo;
import com.playtheatria.sessions.errors.PersistenceException;
import java.time.LocalDate;
import java.util.logging.Level;

public class DailyStatsService {
    private final DailyStatsCache cache;
    private final DailyStatsRepo repo;

    public DailyStatsService(DailyStatsCache cache, DailyStatsRepo repo) {
        this.cache = cache;
        this.repo = repo;
    }

    public int incrementRewardsEarned() {
        return cache.get().incrementRewardsEarned();
    }

    public int getRewardsEarned() {
        return cache.get().getRewardsEarned();
    }

    public LocalDate getDate() {
        return cache.get().getDate();
    }

    public int getPlayersJoined() {
        return cache.get().getPlayersJoined();
    }

    public void reset() {
        printDailyStatsDebugLogs(cache.get());

        logger.log(Level.INFO, "Setting new DailyStats for today.");
        cache.set(new DailyStats(LocalDate.now(TimeUtils.timeZone)));
        printDailyStatsDebugLogs(cache.get());

        logger.log(Level.INFO, "Purging DailyStatsRepo.");
        switch (repo.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> logger.log(
                    Level.INFO, String.format("Deleted" + " %d entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> logger.log(
                    Level.INFO,
                    String.format("Purging DailyStats failed %s", err.error().getMessage()));
        }
    }

    public void setPlayersJoined(int count) {
        cache.get().setPlayersJoined(count);
    }

    public void persist(boolean verbose) {
        logger.log(
                Level.INFO, "[persist] Running on thread: {0}", Thread.currentThread().getName());
        switch (repo.createOrUpdate(cache.get())) {
            case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                Dao.CreateOrUpdateStatus status = ok.value();
                String msg =
                        String.format(
                                "DailyStats persisted successfully | created: %s, updated: %s,"
                                        + " lines updated: %s",
                                status.isCreated(),
                                status.isUpdated(),
                                status.getNumLinesChanged());
                if (verbose) {
                    logger.log(Level.INFO, msg);
                } else {
                    logger.log(Level.INFO, msg);
                }
            }
            case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> logger.log(
                    Level.SEVERE,
                    String.format("Error persisting DailyStats: %s", err.error().getMessage()));
        }
    }

    /**
     * Prints debug logs for the provided DailyStats
     * @param stats DailyStats to print debug logs for
     */
    public void printDailyStatsDebugLogs(DailyStats stats) {
        logger.log(
                Level.INFO,
                String.format(
                        "Date %s\nRewardsEarned %s\nPlayersJoined %s",
                        stats.getDate(), stats.getRewardsEarned(), stats.getPlayersJoined()));
    }
}
