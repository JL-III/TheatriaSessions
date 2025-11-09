package com.playtheatria.sessions.listeners;

import java.time.LocalDate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.database.repositories.DailyStatsRepo;
import com.playtheatria.sessions.database.repositories.SessionRepository;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.managers.DailyStatsCache;
import com.playtheatria.sessions.managers.SessionCache;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.theatriaTime.events.DayChangeEvent;

public class DayChange implements Listener {
    private final SessionRepository sessionRepository;
    private final SessionCache sessionCache;
    private final DailyStatsRepo dailyStatsRepo;
    private final DailyStatsCache dailyStatsCache;
    private final PLog log;

    public DayChange(
            SessionRepository sessionRepository,
            DailyStatsRepo dailyStatsRepo,
            SessionCache sessionCache,
            DailyStatsCache dailyStatsCache,
            PLog log) {
        this.sessionRepository = sessionRepository;
        this.dailyStatsRepo = dailyStatsRepo;
        this.dailyStatsCache = dailyStatsCache;
        this.sessionCache = sessionCache;
        this.log = log;
    }

    /**
     * Handles DayChangeEvent to reset sessions and server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetSessions(DayChangeEvent event) {
        log.debug("[DayChangeEvent] Resetting sessions.");
        sessionCache.resetSessions();
        switch (sessionRepository.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> log.debug(
                    String.format("Deleted %d" + " entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> log.debug(
                    String.format("Purging SessionRepository failed %s", err.error().getMessage()));
        }
    }

    /**
     * Handles DayChangeEvent to reset server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetDailyStats(DayChangeEvent event) {
        log.debug("[DayChangeEvent] resetting DailyStats.");
        log.debug("Logs for DailyStats");
        printDailyStatsDebugLogs(dailyStatsCache.getDayStats());

        log.debug("Setting new DailyStats for today.");
        dailyStatsCache.setDayStats(new DailyStats(LocalDate.now(TimeUtils.timeZone)));
        printDailyStatsDebugLogs(dailyStatsCache.getDayStats());

        log.debug("Purging DailyStatsRepo.");
        switch (dailyStatsRepo.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> log.debug(
                    String.format("Deleted" + " %d entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> log.debug(
                    String.format("Purging DailyStats failed %s", err.error().getMessage()));
        }
    }

    /**
     * Prints debug logs for the provided DailyStats
     * @param dailyStats DailyStats to print debug logs for
     */
    private void printDailyStatsDebugLogs(DailyStats dailyStats) {
        log.debug(String.format("Date %s", dailyStats.getDate()));
        log.debug(String.format("RewardsEarned %s", dailyStats.getRewardsEarned()));
        log.debug(String.format("PlayersJoined %s", dailyStats.getPlayersJoined()));
    }
}
