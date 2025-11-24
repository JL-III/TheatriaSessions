package com.playtheatria.sessions.service;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.cache.StreakCache;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.database.repositories.StreakRepo;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.utils.PLog;
import java.util.Collection;
import java.util.UUID;

public class StreakService {
    private final StreakCache cache;
    private final StreakRepo repo;
    private final PLog log;

    public StreakService(StreakCache cache, StreakRepo repo, PLog log) {
        this.cache = cache;
        this.repo = repo;
        this.log = log;
    }

    public Result<Streak, Exception> getStreak(UUID playerUUUID) {
        return cache.getStreak(playerUUUID);
    }

    public boolean hasStreak(UUID playerUUID) {
        return cache.hasStreak(playerUUID);
    }

    public void createNewStreak(UUID playerUUID, String playerName) {
        cache.createNewStreak(playerUUID, playerName);
    }

    public Collection<Streak> getStreaks() {
        return cache.getStreaks().values();
    }

    public void persist(boolean verbose) {
        log.debugFmt("[persist] Running on thread: %s", Thread.currentThread().getName());
        for (Streak streak : getStreaks()) {
            switch (repo.createOrUpdate(streak)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                    Dao.CreateOrUpdateStatus status = ok.value();
                    String msg =
                            String.format(
                                    "Persisted streak - Created: %b, Updated: %b, Lines changed:"
                                            + " %d. ",
                                    status.isCreated(),
                                    status.isUpdated(),
                                    status.getNumLinesChanged());
                    if (verbose) {
                        log.info(msg + streak);
                    } else {
                        log.debug(msg + streak);
                    }
                }
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> log.err(
                        String.format(
                                "Persisting streak %s failed: %s",
                                streak, err.error().getMessage()));
            }
        }
    }
}
