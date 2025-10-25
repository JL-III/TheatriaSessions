package com.playtheatria.theatriaSessions.database.data;

import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "streaks")
public class Streak {
    @DatabaseField(id = true, index = true)
    private UUID playerUUID;

    @DatabaseField
    private Integer currentStreak;

    @DatabaseField
    private Integer longestStreak;

    protected Streak() { }

    /**
     * Initializes a Streak object for a player with a given UUID.
     * Both currentStreak and longestStreak are initialized to 0.
     * This is only initialized once per player, when they first join the server.
     * @param playerUUID The UUID of the player.
     */
    public Streak(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.currentStreak = 0;
        this.longestStreak = 0;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }
}
