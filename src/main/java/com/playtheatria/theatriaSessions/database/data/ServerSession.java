package com.playtheatria.theatriaSessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDate;

@DatabaseTable(tableName = "server_session")
public class ServerSession {

    @DatabaseField(id = true)
    private final int id = 0;

    @DatabaseField
    private String sessionDate;

    @DatabaseField
    private int playersJoined = 0;

    @DatabaseField
    private int rewardsEarned = 0;

    protected ServerSession() {
    }

    public ServerSession(LocalDate date) {
        this.sessionDate = date.toString();
    }

    public LocalDate getSessionDate() {
        return LocalDate.parse(sessionDate);
    }

    public int getPlayersJoined() {
        return playersJoined;
    }

    public void setPlayersJoined(int amount) {
        playersJoined = amount;
    }

    public int getRewardsEarned() {
        return rewardsEarned;
    }

    public void incrementRewardsEarned() {
        rewardsEarned++;
    }
}
