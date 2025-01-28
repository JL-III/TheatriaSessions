package com.playtheatria.theatriaSessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.commands.CommunityCommand;
import com.playtheatria.theatriaSessions.commands.SessionCommand;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.data.ServerSession;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.listeners.*;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.tasks.DatabaseTask;
import com.playtheatria.theatriaSessions.tasks.OneSecondTimerTask;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class TheatriaSessions extends JavaPlugin {
    private SessionManager sessionManager;
    private ServerSessionManager serverSessionManager;
    private SessionRepository sessionRepository;
    private ServerSessionRepository serverSessionRepository;
    private CustomLogger<TheatriaSessions, ConfigManager> customLogger;
    private DatabaseTask databaseTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this);
        customLogger = new CustomLogger<>(
                configManager,
                Util.COLOR_ONE,
                Util.COLOR_TWO,
                Util.COLOR_THREE
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
            theatriaSessionsDB = new TheatriaSessionsDB(getDataFolder(), customLogger);
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

        try {
            serverSessionRepository = new ServerSessionRepository(theatriaSessionsDB, customLogger);
        } catch (SQLException e) {
            customLogger.sendFormattedLog("Failed to create ServerSessionRepository: " + e.getMessage());
            customLogger.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        serverSessionManager = new ServerSessionManager(serverSessionRepository.loadServerSession());

        databaseTask = new DatabaseTask(sessionRepository, serverSessionRepository, sessionManager, serverSessionManager, customLogger);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(this, 20 * configManager.getInitialBackupDuration(), 20 * configManager.getBackupDuration());
        new OneSecondTimerTask(sessionManager, serverSessionManager, essentials).runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DayChangeListener(sessionRepository, serverSessionRepository, sessionManager, serverSessionManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(sessionManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayerListener(configManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new IncrementRewardCountListener(serverSessionManager, customLogger), this);
        Bukkit.getPluginManager().registerEvents(new RewardCommunityListener(customLogger, configManager), this);
        Objects.requireNonNull(getCommand("session")).setExecutor(new SessionCommand(sessionManager, configManager, customLogger));
        Objects.requireNonNull(getCommand("community")).setExecutor(new CommunityCommand(serverSessionManager, customLogger, configManager));
        customLogger.sendFormattedLog("Loaded plugin.");
        customLogger.sendFormattedLog("Using GeneralUtils version: " + customLogger.getGeneralUtilsVersionFromConfig(getResource("plugin.yml"), "general-utils-version"));
        customLogger.sendFormattedLog("Using TheatriaTime version: " + customLogger.getGeneralUtilsVersionFromConfig(getResource("plugin.yml"), "theatria-time-version"));
    }

    @Override
    public void onDisable() {
        if (databaseTask != null) databaseTask.cancel();
        if (sessionManager != null && sessionRepository != null) {
            for (Session session : sessionManager.getSessions().values()) {
                customLogger.sendFormattedLog("User: " + session.getPlayerName() + " had a session time of " + session.getSessionTime());
                sessionRepository.createOrUpdate(session);
            }
        }
        if (serverSessionManager != null && serverSessionRepository != null) {
            ServerSession serverSession = serverSessionManager.getServerSession();
            customLogger.sendFormattedLog(String.format("ServerSession Date: %s, RewardsEarned: %s, PlayersJoined: %s", serverSession.getSessionDate(), serverSession.getRewardsEarned(), serverSession.getPlayersJoined()));
            serverSessionRepository.createOrUpdate(serverSession);
        }
    }
}
