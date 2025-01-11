package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class PriceRepository {
    private final Dao<Price, String> dao;
    private final CustomLogger customLogger;

    Map<String, Duration> retentionPeriods = Map.of(
            "HOURLY", Duration.ofHours(24),
            "DAILY", Duration.ofDays(7),
            "WEEKLY", Duration.ofDays(35),
            "MONTHLY", Duration.ofDays(365),
            "YEARLY", Duration.ofDays(3650)
    );

    public PriceRepository(TheatriaSessionsDB theatriaSessionsDB, CustomLogger customLogger) throws SQLException {
        this.dao = theatriaSessionsDB.getDao(Price.class);
        this.customLogger = customLogger;
    }

    public void cleanupTask() {
        for (Map.Entry<String, Duration> entry : retentionPeriods.entrySet()) {
            String historyType = entry.getKey();
            Duration duration = entry.getValue();

            LocalDateTime cutoff = LocalDateTime.now(Util.timeZone).minus(duration);
            String query = "DELETE FROM prices WHERE historyType = ? AND timestamp < ?";
            try {
                dao.executeRaw(query, historyType, cutoff.toString());
                customLogger.sendDebug(String.format("executed: %s %s %s", query, historyType, cutoff));
            } catch (SQLException exception) {
                Util.sendFormattedLog(String.format("Failed to clean up historyType '%s' with cutoff '%s': %s",
                        historyType, cutoff, exception.getMessage()));
                exception.printStackTrace();
            }
        }
    }

    public void save(Price price) {
        try {
            customLogger.sendDebug(String.format("Saving price to database for: %s %s %s %s", price.getHistoryType(), price.getMaterial(), price.getPrice(), price.getTimestamp()));
            dao.create(price);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
