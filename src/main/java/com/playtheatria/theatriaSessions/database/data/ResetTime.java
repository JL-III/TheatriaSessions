package com.playtheatria.theatriaSessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.playtheatria.theatriaSessions.utils.Util;

import java.time.LocalDateTime;
import java.time.temporal.IsoFields;

@DatabaseTable(tableName = "reset_time")
public class ResetTime {

    @DatabaseField(id = true)
    private final int id;

    @DatabaseField
    private final String lastResetHour; // ISO 8601 string for the last reset

    @DatabaseField
    private final String nextResetHour; // ISO 8601 string for the next reset

    public ResetTime() {
        this.id = 0;
        LocalDateTime now = LocalDateTime.now(Util.timeZone);
        this.lastResetHour = now.toString();
        this.nextResetHour = calculateNextResetHour(now).toString();
    }

    public ResetTime(LocalDateTime manuallySetTime) {
        this.id = 0;
        this.lastResetHour = manuallySetTime.toString();
        this.nextResetHour = calculateNextResetHour(manuallySetTime).toString();
    }

    public LocalDateTime getLastResetHour() {
        return LocalDateTime.parse(lastResetHour);
    }

    public LocalDateTime getNextResetHour() {
        return LocalDateTime.parse(nextResetHour);
    }

    public LocalDateTime calculateNextResetHour(LocalDateTime lastResetHour) {
        return lastResetHour.plusHours(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    public boolean isNewDay(LocalDateTime now) {
        return getLastResetHour().getDayOfYear() != now.getDayOfYear();
    }

    public boolean isNewWeek(LocalDateTime now) {
        return getLastResetHour().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) != now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    public boolean isNewMonth(LocalDateTime now) {
        return getLastResetHour().getMonthValue() != now.getMonthValue();
    }

    public boolean isNewYear(LocalDateTime now) {
        return getLastResetHour().getYear() != now.getYear();
    }
}

