package com.playtheatria.theatriaSessions.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.db.SqliteDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class TheatriaSessionsDB {
    private static final String DATABASE_NAME = "TheatriaSessions.db";
    private static final String URI_STRING = "jdbc:sqlite:%s";
    private final File file;

    public TheatriaSessionsDB(File dataFolder, CustomLogger<TheatriaSessions, ConfigManager> logger)
            throws IOException {
        File databaseFile = new File(dataFolder, DATABASE_NAME);
        if (databaseFile.createNewFile()) {
            logger.sendFormattedLog(
                    String.format(
                            "Failed to find database file within %s named %s",
                            dataFolder, DATABASE_NAME));
        } else {
            logger.sendFormattedLog(
                    String.format(
                            "Found database file within %s named %s", dataFolder, DATABASE_NAME));
        }
        this.file = databaseFile;
    }

    public <E, I> Dao<E, I> getDao(Class<E> entity) throws SQLException {
        ConnectionSource conn =
                new JdbcConnectionSource(
                        String.format(URI_STRING, file.getAbsolutePath()),
                        new SqliteDatabaseType());
        Dao<E, I> dao = DaoManager.createDao(conn, entity);
        TableUtils.createTableIfNotExists(dao.getConnectionSource(), entity);
        return dao;
    }
}
