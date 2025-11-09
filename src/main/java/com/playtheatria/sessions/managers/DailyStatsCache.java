package com.playtheatria.sessions.managers;

import org.jetbrains.annotations.NotNull;

import com.playtheatria.sessions.database.data.DailyStats;

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
