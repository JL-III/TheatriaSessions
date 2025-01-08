package com.playtheatria.theatriaSessions.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDateTime;
import java.time.ZoneId;

@DatabaseTable(tableName = "reset_time")
public class ResetTime {
    @DatabaseField(id = true)
    private final int id;

    @DatabaseField
    private final String lastResetTime;

    @DatabaseField
    private final String nextResetTime;

    /**
     * Call this method to create a new ResetTime for the ResetTimeManager to hold in memory.
     * DatabaseTask then reads and propagates to the ResetTimeRepository.
     */
    public ResetTime() {
        this.id = 0;
        LocalDateTime now = LocalDateTime.now();
        this.lastResetTime = now.toString();
        this.nextResetTime = calculateNextResetTime(now).toString();
    }

    /**
     * Normally you should call the 0 args constructor.
     * This method allows for testing or a force reset to occur.
     * @param manuallySetTime a time provided in order to manually set a time.
     */
    public ResetTime(LocalDateTime manuallySetTime) {
        this.id = 0;
        this.lastResetTime = manuallySetTime.toString();
        this.nextResetTime = calculateNextResetTime(manuallySetTime).toString();
    }

    public LocalDateTime getLastResetTime() {
        return LocalDateTime.parse(lastResetTime);
    }

    public LocalDateTime getNextResetTime() {
        return LocalDateTime.parse(nextResetTime);
    }

    public LocalDateTime calculateNextResetTime(LocalDateTime lastResetTime) {
        return lastResetTime
                .plusDays(1)
                .withHour(3)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .atZone(ZoneId.of("America/New_York"))
                .toLocalDateTime();
    }
}
