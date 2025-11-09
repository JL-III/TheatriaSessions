package com.playtheatria.sessions.managers;

import com.playtheatria.sessions.database.data.DailyStats;
import org.jetbrains.annotations.NotNull;

public class DailyStatsCache {
    private DailyStats dayState;

    public DailyStatsCache(@NotNull DailyStats serverSession) {
        this.dayState = serverSession;
    }

    public void setDayStats(@NotNull DailyStats serverSession) {
        this.dayState = serverSession;
    }

    public @NotNull DailyStats getDayStats() {
        return dayState;
    }
}
