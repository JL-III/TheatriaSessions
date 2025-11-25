package com.playtheatria.sessions.tasks;

import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.PLog;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionService sessionService;
    private final DailyStatsService dailyStatsService;
    private final StreakService streakService;
    private final PLog log;

    public DatabaseTask(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            StreakService streakService,
            PLog log) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.log = log;
    }

    @Override
    public void run() {
        long start = System.nanoTime();
        log.debug("[run] Running on thread: " + Thread.currentThread().getName());
        log.debug("DatabaseTask: " + sessionService.getSessionsCount() + " sessions.");
        sessionService.persist(false);
        dailyStatsService.persist(false);
        streakService.persist(false);

        long end = System.nanoTime();
        long ms = (end - start) / 1_000_000L;

        log.debug("DatabaseTask duration: " + ms + "ms");
    }
}
