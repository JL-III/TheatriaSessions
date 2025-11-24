package com.playtheatria.sessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@DatabaseTable(tableName = "streaks_v2")
public class Streak {
    @DatabaseField(id = true, index = true)
    private UUID playerUUID;

    @DatabaseField private String playerName;

    @DatabaseField private Integer currentStreak;

    @DatabaseField private Integer longestStreak;

    @DatabaseField private String lastEarnedDate;

    protected Streak() {}

    /**
     * Initializes a Streak object for a player with a given UUID.
     * Both currentStreak and longestStreak are initialized to 0.
     * This is only initialized once per player, when they first join the server.
     * @param playerUUID The UUID of the player.
     */
    public Streak(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.lastEarnedDate = null;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreakToOne() {
        this.currentStreak = 1;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void incrementCurrentStreak() {
        this.currentStreak += 1;
        if (this.currentStreak > this.longestStreak) {
            this.longestStreak = this.currentStreak;
        }
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastEarnedDate() throws DateTimeParseException {
        if (lastEarnedDate != null) {
            return LocalDate.parse(lastEarnedDate);
        }
        return null;
    }

    public void setLastEarnedDate(LocalDate date) {
        this.lastEarnedDate = date.toString();
    }
}
