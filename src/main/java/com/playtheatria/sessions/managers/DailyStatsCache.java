package com.playtheatria.sessions.managers;

import com.playtheatria.sessions.database.data.DailyStats;
import org.jetbrains.annotations.NotNull;

public class DailyStatsCache {
    private DailyStats dailyStats;

    public DailyStatsCache(@NotNull DailyStats dailyStats) {
        this.dailyStats = dailyStats;
    }

    public void setDayStats(@NotNull DailyStats dailyStats) {
        this.dailyStats = dailyStats;
    }

    public @NotNull DailyStats getDayStats() {
        return dailyStats;
    }
}
