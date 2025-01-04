package com.playtheatria.theatriaSessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.theatriaSessions.commands.SessionCommand;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.listeners.*;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.tasks.DatabaseTask;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.tasks.SessionTask;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class TheatriaSessions extends JavaPlugin {

    private SessionManager sessionManager;
    private ServerSessionManager serverSessionManager;
    private DatabaseTask databaseTask;
    private SessionRepository sessionRepository;
    private ServerSessionRepository serverSessionRepository;

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
            serverSessionRepository = new ServerSessionRepository(theatriaSessionsDB, customLogger);
        } catch (SQLException e) {
            Util.sendFormattedLog("Failed to create ServerSessionRepository: " + e.getMessage());
            Util.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        sessionManager = new SessionManager(sessionRepository.loadSessions());
        serverSessionManager = new ServerSessionManager(serverSessionRepository.loadServerSession());
        databaseTask = new DatabaseTask(sessionRepository, serverSessionRepository, sessionManager, serverSessionManager);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(this, 20 * configManager.getInitialBackupDuration(), 20 * configManager.getBackupDuration());
        SessionTask sessionTask = new SessionTask(sessionManager, essentials);
        sessionTask.runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DayChangeListener(sessionManager, serverSessionManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(serverSessionManager, sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayerListener(configManager), this);
        Bukkit.getPluginManager().registerEvents(new DatabaseDayChangeListener(sessionRepository), this);
        Bukkit.getPluginManager().registerEvents(new IncrementRewardCountListener(serverSessionManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new RewardCommunityListener(), this);
        Objects.requireNonNull(getCommand("session")).setExecutor(new SessionCommand(serverSessionManager, sessionManager, configManager));
    }

    @Override
    public void onDisable() {
        if (databaseTask != null) {
            databaseTask.cancel();
        }
        if (sessionManager != null && sessionRepository != null) {
            for (Session session : sessionManager.getSessions()) {
                Util.sendFormattedLog("User: " + session.getPlayerName() + " had a session time of " + session.getSessionTime());
            }

            for (Session session : sessionManager.getSessions()) {
                sessionRepository.createOrUpdate(session);
            }
        }
        if (serverSessionManager != null && serverSessionRepository != null) {
            serverSessionRepository.createOrUpdate(serverSessionManager.getServerSession());
            Util.sendFormattedLog("ServerSession info: " + serverSessionManager.getServerSession().getSessionDate() + " playersJoined: " + serverSessionManager.getServerSession().getPlayersJoined() + " | rewardsEarned: " + serverSessionManager.getServerSession().getRewardsEarned());
        }
    }
}
