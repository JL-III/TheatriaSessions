package com.playtheatria.theatriaSessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.theatriaSessions.commands.SessionCommand;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.database.TheatriaSessionsDB;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.listeners.DatabaseDayChangeListener;
import com.playtheatria.theatriaSessions.listeners.DayChangeListener;
import com.playtheatria.theatriaSessions.listeners.PlayerJoinListener;
import com.playtheatria.theatriaSessions.listeners.RewardPlayerListener;
import com.playtheatria.theatriaSessions.tasks.DatabaseTask;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.tasks.SessionTask;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public final class TheatriaSessions extends JavaPlugin {

    private SessionManager sessionManager;
    private DatabaseTask databaseTask;
    private SessionRepository sessionRepository;

    @Override
    public void onEnable() {
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
            sessionRepository = new SessionRepository(theatriaSessionsDB);
        } catch (SQLException e) {
            Util.sendFormattedLog("Failed to create sessionRepository: " + e.getMessage());
            Util.sendFormattedLog("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        sessionManager = new SessionManager(sessionRepository.loadSessions());
        databaseTask = new DatabaseTask(sessionRepository, sessionManager);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(this, 20 * 600, 20 * 600);
        SessionTask sessionTask = new SessionTask(sessionManager, essentials);
        sessionTask.runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager().registerEvents(new DayChangeListener(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new DatabaseDayChangeListener(sessionRepository), this);
        Objects.requireNonNull(getCommand("session")).setExecutor(new SessionCommand(sessionManager));
    }

    @Override
    public void onDisable() {
        if (databaseTask != null && sessionManager != null && sessionRepository != null) {
            for (Session session : sessionManager.getSessions()) {
                Util.sendFormattedLog("User: " + session.getPlayerName() + " had a session time of " + session.getSessionTime());
            }
            databaseTask.cancel();

            for (Session session : sessionManager.getSessions()) {
                sessionRepository.createOrUpdate(session);
            }
        }
    }
}
