package com.playtheatria.sessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.UUID;

@DatabaseTable(tableName = "sessions")
public class Session {
    @DatabaseField(id = true, index = true)
    private UUID playerUUID;

    @DatabaseField private String playerName;

    @DatabaseField private Integer sessionTime = 0;

    @DatabaseField private Integer afkTime = 0;

    @DatabaseField public final Integer THRESHOLD = 3600;

    @DatabaseField private boolean rewarded = false;

    protected Session() {}

    public Session(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    /**
     * If the session time has exceeded the threshold, the player is ready to receive their reward.
     * @return whether the session has exceeded the threshold
     */
    public boolean hasEarnedReward() {
        return sessionTime >= THRESHOLD;
    }

    public Integer getSessionTime() {
        return this.sessionTime;
    }

    public void setSessionTime(Integer amount) {
        this.sessionTime = amount;
    }

    public int incrementSessionTime() {
        return ++sessionTime;
    }

    public void incrementAfkTime() {
        afkTime++;
    }

    public void setRewarded() {
        this.rewarded = true;
    }

    public boolean isRewarded() {
        return this.rewarded;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public Integer getAfkTime() {
        return this.afkTime;
    }

    @Override
    public String toString() {
        return "Session{playerName=%s, playerUUID=%s, sessionTime=%d, rewarded=%b}"
                .formatted(playerName, playerUUID, sessionTime, rewarded);
    }
}
