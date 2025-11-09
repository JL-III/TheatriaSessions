package com.playtheatria.sessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.time.LocalDate;

@DatabaseTable(tableName = "daily_stats")
public class DailyStats {

    @DatabaseField(id = true)
    @SuppressWarnings("unused")
    private final int id = 0;

    @DatabaseField private String date;

    @DatabaseField private int playersJoined = 0;

    @DatabaseField private int rewardsEarned = 0;

    protected DailyStats() {}

    public DailyStats(LocalDate date) {
        this.date = date.toString();
    }

    public LocalDate getDate() {
        return LocalDate.parse(date);
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
