package com.playtheatria.theatriaSessions.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDateTime;

@DatabaseTable(tableName = "reset_time")
public class ResetTime {
    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private String lastResetTime;

    @DatabaseField
    private String nextResetTime;

    public ResetTime() {
        this.id = 0;
        this.lastResetTime = LocalDateTime.now().toString();
        this.nextResetTime = calculateNextResetTime(LocalDateTime.now()).toString();
    }

    public ResetTime(LocalDateTime now) {
        this.id = 0;
        this.lastResetTime = now.toString();
        this.nextResetTime = calculateNextResetTime(now).toString();
    }

    public LocalDateTime getLastResetTime() {
        return LocalDateTime.parse(lastResetTime);
    }

    public void setLastResetTime(LocalDateTime lastResetTime) {
        this.lastResetTime = lastResetTime.toString();
    }

    public LocalDateTime getNextResetTime() {
        return LocalDateTime.parse(nextResetTime);
    }

    public LocalDateTime calculateNextResetTime(LocalDateTime now) {
        return now.plusDays(1)
                .withHour(8)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    public void setNextResetTime(LocalDateTime now) {
        this.nextResetTime = calculateNextResetTime(now).toString();
    }
}
