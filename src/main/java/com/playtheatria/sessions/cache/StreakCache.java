package com.playtheatria.sessions.cache;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.utils.PLog;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class StreakCache {
    private final ConcurrentHashMap<UUID, Streak> mappedStreaks = new ConcurrentHashMap<>();
    private final PLog logger;

    public StreakCache(List<Streak> streaks, PLog logger) {
        for (Streak streak : streaks) {
            mappedStreaks.put(streak.getPlayerUUID(), streak);
        }
        this.logger = logger;
    }

    public boolean hasStreak(@NotNull UUID playerUUID) {
        return mappedStreaks.get(playerUUID) != null;
    }

    public Result<Streak, Exception> getStreak(@NotNull UUID playerUUID) {
        Streak streak = mappedStreaks.get(playerUUID);
        if (streak == null) {
            return new Err<>(
                    new Exception(
                            String.format(
                                    "Failed to return a streak from StreakCache for UUID: %s",
                                    playerUUID)));
        }
        return new Ok<>(streak);
    }

    public ConcurrentHashMap<UUID, Streak> getStreaks() {
        return mappedStreaks;
    }

    public void createNewStreak(@NotNull UUID playerUUID, @NotNull String playerName) {
        logger.debugFmt("Creating streak for %s", playerName);
        mappedStreaks.put(playerUUID, new Streak(playerUUID, playerName));
    }

    public void addStreak(@NotNull Streak streak) {
        mappedStreaks.put(streak.getPlayerUUID(), streak);
    }
}
