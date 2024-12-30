package com.playtheatria.theatriaSessions.data;

import java.util.UUID;

public class Session {
    private final UUID playerUUID;
    private final String playerName;
    private Integer sessionTime = 0;
    public final Integer THRESHOLD = 3600;
    private boolean rewarded = false;


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

    public void incrementSessionTime() {
        sessionTime++;
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
}
