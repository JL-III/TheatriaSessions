package com.playtheatria.theatriaSessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.theatriaSessions.commands.SessionCommand;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.ResetTime;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.repositories.ResetTimeRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.listeners.DatabaseDayChangeListener;
import com.playtheatria.theatriaSessions.listeners.DayChangeListener;
import com.playtheatria.theatriaSessions.listeners.PlayerJoinListener;
import com.playtheatria.theatriaSessions.listeners.RewardPlayerListener;
import com.playtheatria.theatriaSessions.managers.ResetTimeManager;
import com.playtheatria.theatriaSessions.tasks.DatabaseTask;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.tasks.OneSecondTimerTask;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class TheatriaSessions extends JavaPlugin {

    private SessionManager sessionManager;
    private SessionRepository sessionRepository;
    private ResetTimeManager resetTimeManager;
    private ResetTimeRepository resetTimeRepository;
    private DatabaseTask databaseTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this);
        CustomLogger customLogger = new CustomLogger(configManager);
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            Util.sendFormattedLog("Essentials returned null, shutting down.");
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
            Util.sendFormattedLog("Failed to create database: " + e.getMessage());
            e.printStackTrace();
            Util.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            sessionRepository = new SessionRepository(theatriaSessionsDB, customLogger);
        } catch (SQLException e) {
            Util.sendFormattedLog("Failed to create sessionRepository: " + e.getMessage());
            Util.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            resetTimeRepository = new ResetTimeRepository(theatriaSessionsDB, customLogger);
        } catch (SQLException e) {
            Util.sendFormattedLog("Failed to create sessionRepository: " + e.getMessage());
            Util.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        sessionManager = new SessionManager(sessionRepository.loadSessions());
        // If there is an exception
        ResetTime resetTime = resetTimeRepository.loadResetTime();
        if (resetTime == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            Util.sendFormattedLog("ResetTime returned null, this needs to be addressed. Shutting down the plugin.");
            return;
        }
        resetTimeManager = new ResetTimeManager(resetTime);
        databaseTask = new DatabaseTask(resetTimeRepository, resetTimeManager, sessionRepository, sessionManager);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(this, 20 * configManager.getInitialBackupDuration(), 20 * configManager.getBackupDuration());
        OneSecondTimerTask oneSecondTimerTask = new OneSecondTimerTask(resetTimeManager, sessionManager, essentials);
        oneSecondTimerTask.runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DayChangeListener(resetTimeManager, sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayerListener(configManager), this);
        Bukkit.getPluginManager().registerEvents(new DatabaseDayChangeListener(sessionRepository), this);
        Objects.requireNonNull(getCommand("session")).setExecutor(new SessionCommand(resetTimeManager, sessionManager, configManager));
    }

    @Override
    public void onDisable() {
        if (databaseTask != null && sessionManager != null && sessionRepository != null) {
            databaseTask.cancel();
            for (Session session : sessionManager.getSessions().values()) {
                Util.sendFormattedLog("User: " + session.getPlayerName() + " had a session time of " + session.getSessionTime());
                sessionRepository.createOrUpdate(session);
            }
        }
        if (resetTimeRepository != null && resetTimeManager != null) {
            resetTimeRepository.saveResetTime(resetTimeManager.getResetTime());
        }
    }
}
