package com.playtheatria.theatriaSessions.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDateTime;

@DatabaseTable(tableName = "reset_times")
public class ResetTime {

    @DatabaseField(id = true) // Primary key
    private int id;

    @DatabaseField
    private String lastResetTime;

    public ResetTime() {
        this.id = 0; // Always enforce a single row with id = 0
    }

    /**
     * This method is used to construct a new database entry, it is used after a day change has been determined
     * and resets have been triggered. The ID is set to 0 since this is a one row database.
     * @param lastResetTime When a day change is determined, provide the lastResetTime here.
     */
    public ResetTime(LocalDateTime lastResetTime) {
        this.id = 0; // Always enforce a single row with id = 0
        this.lastResetTime = lastResetTime.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getLastResetTime() {
        return LocalDateTime.parse(lastResetTime);
    }

    public void setLastResetTime(LocalDateTime lastResetTime) {
        this.lastResetTime = lastResetTime.toString();
    }
}

