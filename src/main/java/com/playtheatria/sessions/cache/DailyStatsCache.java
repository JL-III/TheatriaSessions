package com.playtheatria.sessions.cache;

import com.playtheatria.sessions.database.data.DailyStats;
import org.jetbrains.annotations.NotNull;

public class DailyStatsCache {
    private DailyStats dailyStats;

    public DailyStatsCache(@NotNull DailyStats dailyStats) {
        this.dailyStats = dailyStats;
    }

    public void set(@NotNull DailyStats dailyStats) {
        this.dailyStats = dailyStats;
    }

    public @NotNull DailyStats get() {
        return dailyStats;
    }
}
