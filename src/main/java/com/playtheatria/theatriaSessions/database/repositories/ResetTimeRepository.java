package com.playtheatria.theatriaSessions.database.repositories;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.theatriaSessions.data.ResetTime;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;

import java.sql.SQLException;

public class ResetTimeRepository {
    private final Dao<ResetTime, String> dao;
    private final CustomLogger customLogger;

    public ResetTimeRepository(TheatriaSessionsDB theatriaSessionsDB, CustomLogger customLogger) throws SQLException {
        this.dao = theatriaSessionsDB.getDao(ResetTime.class);
        this.customLogger = customLogger;
    }

    public ResetTime loadResetTime() {
        try {
            ResetTime resetTime = dao.queryForId("0");
            if (resetTime == null) {
                Util.sendFormattedLog("No ResetTime found in database. Creating a new ResetTime.");
                resetTime = new ResetTime();
                dao.create(resetTime);
            }
            return resetTime;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void saveResetTime(ResetTime resetTime) {
        try {
            ResetTime existing = dao.queryForId("0");
            if (existing == null) {
                Util.sendFormattedLog("No ResetTime found. Creating a new ResetTime.");
                dao.create(resetTime);
            } else {
                dao.update(resetTime);
                customLogger.sendDebug("Backing up reset time, this is normal");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
