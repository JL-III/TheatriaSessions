package com.playtheatria.sessions.tasks;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;

import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionService sessionService;
    private final DailyStatsService dailyStatsService;
    private static final Logger logger = Logger.getLogger(DatabaseTask.class.getName());

    public DatabaseTask(
            DailyStatsService dailyStatsService, SessionService sessionService) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        logger.log(Level.INFO, "[run] Running on thread: {0}", Thread.currentThread().getName());
        logger.log(Level.INFO, "DatabaseTask: {0} sessions.", sessionService.getSessionsCount());
        sessionService.persist(false);
        dailyStatsService.persist(false);

        long end = System.nanoTime();
        long ms = (end - start) / 1_000_000L;

        logger.log(Level.INFO, "DatabaseTask duration: {0}ms", ms);
    }
}
