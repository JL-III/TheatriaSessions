package com.playtheatria.theatriaSessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.commands.SessionCommand;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.listeners.DayChangeListener;
import com.playtheatria.theatriaSessions.listeners.PlayerJoinListener;
import com.playtheatria.theatriaSessions.listeners.RewardPlayerListener;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.tasks.DatabaseTask;
import com.playtheatria.theatriaSessions.tasks.OneSecondTimerTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class TheatriaSessions extends JavaPlugin {
    private SessionManager sessionManager;
    private SessionRepository sessionRepository;
    private CustomLogger<TheatriaSessions, ConfigManager> customLogger;
    private DatabaseTask databaseTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this);
        customLogger = new CustomLogger<>(
                configManager,
                "#f5428a",
                "#42f598",
                "#fff8bd"
        );
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            customLogger.sendFormattedLog("Essentials returned null, shutting down.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        TheatriaSessionsDB theatriaSessionsDB;
        // Ensure the data folder exists
        if (!getDataFolder().exists()) {
            boolean created = getDataFolder().mkdirs();
            if (created) {
                getLogger().info("Plugin data folder created at: " + getDataFolder().getAbsolutePath());
            } else {
                getLogger().severe("Failed to create plugin data folder: " + getDataFolder().getAbsolutePath());
            }
        }
        try {
            theatriaSessionsDB = new TheatriaSessionsDB(getDataFolder());
        } catch (IOException e) {
            customLogger.sendFormattedLog("Failed to create database: " + e.getMessage());
            e.printStackTrace();
            customLogger.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            sessionRepository = new SessionRepository(theatriaSessionsDB, customLogger);
        } catch (SQLException e) {
            customLogger.sendFormattedLog("Failed to create sessionRepository: " + e.getMessage());
            customLogger.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }


        sessionManager = new SessionManager(sessionRepository.loadSessions(), customLogger);
        // If there is an exception

        databaseTask = new DatabaseTask(sessionRepository, sessionManager, customLogger);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(this, 20 * configManager.getInitialBackupDuration(), 20 * configManager.getBackupDuration());
        OneSecondTimerTask oneSecondTimerTask = new OneSecondTimerTask(sessionManager, essentials);
        oneSecondTimerTask.runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DayChangeListener(sessionRepository, sessionManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(sessionManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayerListener(configManager, customLogger), this);
        Objects.requireNonNull(getCommand("session")).setExecutor(new SessionCommand(sessionManager, configManager, customLogger));
    }

    @Override
    public void onDisable() {
        if (databaseTask != null && sessionManager != null && sessionRepository != null) {
            databaseTask.cancel();
            for (Session session : sessionManager.getSessions().values()) {
                customLogger.sendFormattedLog("User: " + session.getPlayerName() + " had a session time of " + session.getSessionTime());
                sessionRepository.createOrUpdate(session);
            }
        }
    }
}
