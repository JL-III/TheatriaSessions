package com.playtheatria.sessions.tasks;

import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.PLog;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionService sessionService;
    private final DailyStatsService dailyStatsService;
    private final PLog log;

    public DatabaseTask(
            DailyStatsService dailyStatsService, SessionService sessionService, PLog log) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.log = log;
    }

    @Override
    public void run() {
        log.debug("DatabaseTask: " + sessionService.getSessionsCount() + " sessions.");
        sessionService.persist();
        dailyStatsService.persist();
    }
}
