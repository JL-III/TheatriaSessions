package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChange implements Listener {
    private final DailyStatsService dailyStatsService;
    private final SessionService sessionService;
    private final PLog log;

    public DayChange(DailyStatsService dailyStatsService, SessionService sessionService, PLog log) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.log = log;
    }

    /**
     * Handles DayChangeEvent to reset sessions and server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetSessions(DayChangeEvent event) {
        log.debug("[DayChangeEvent] Resetting sessions.");
        sessionService.reset();
    }

    /**
     * Handles DayChangeEvent to reset server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetDailyStats(DayChangeEvent event) {
        log.debug("[DayChangeEvent] resetting DailyStats.\nLogs for DailyStats");
        dailyStatsService.reset();
    }
}
