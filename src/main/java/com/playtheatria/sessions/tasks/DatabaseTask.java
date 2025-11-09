package com.playtheatria.sessions.tasks;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.DailyStatsRepo;
import com.playtheatria.sessions.database.repositories.SessionRepository;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.managers.DailyStatsCache;
import com.playtheatria.sessions.managers.SessionCache;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionRepository sessionRepository;
    private final DailyStatsRepo dailyStatsRepo;
    private final SessionCache sessionCache;
    private final DailyStatsCache dailyStatsCache;
    private final PLog log;

    public DatabaseTask(
            SessionRepository sessionRepository,
            DailyStatsRepo dailyStatsRepo,
            SessionCache sessionCache,
            DailyStatsCache dailyStatsCache,
            PLog log) {
        this.sessionRepository = sessionRepository;
        this.sessionCache = sessionCache;
        this.dailyStatsRepo = dailyStatsRepo;
        this.dailyStatsCache = dailyStatsCache;
        this.log = log;
    }

    @Override
    public void run() {
        log.debug("DatabaseTask: " + sessionCache.getSessions().size() + " sessions.");
        for (Session session : sessionCache.getSessions().values()) {
            switch (sessionRepository.createOrUpdate(session)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                    log.debug(
                            Util.summary(
                                    String.format("Session persisted successfully: %s", ok.value()),
                                    session));
                }
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> {
                    log.debug(
                            Util.summary(
                                    String.format(
                                            "Error persisting session: %s",
                                            err.error().getMessage()),
                                    session));
                }
            }
        }

        switch (dailyStatsRepo.createOrUpdate(dailyStatsCache.getDayStats())) {
            case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> log.debug(
                    String.format("DailyStats persisted successfully: %s", ok.value()));
            case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> log.info(
                    String.format("Error persisting DailyStats: %s", err.error().getMessage()));
        }
    }
}
