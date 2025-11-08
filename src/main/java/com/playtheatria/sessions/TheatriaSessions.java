package com.playtheatria.sessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.commands.ActivityCommand;
import com.playtheatria.sessions.commands.CommunityCommand;
import com.playtheatria.sessions.commands.SessionCommand;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.ServerSession;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.ServerSessionRepository;
import com.playtheatria.sessions.database.repositories.SessionRepository;
import com.playtheatria.sessions.errors.RepositoryException;
import com.playtheatria.sessions.listeners.DayChange;
import com.playtheatria.sessions.listeners.PlayerJoin;
import com.playtheatria.sessions.listeners.RewardCommunity;
import com.playtheatria.sessions.listeners.RewardPlayer;
import com.playtheatria.sessions.listeners.ServerSessionRewardCount;
import com.playtheatria.sessions.managers.ServerSessionManager;
import com.playtheatria.sessions.managers.SessionManager;
import com.playtheatria.sessions.tasks.DatabaseTask;
import com.playtheatria.sessions.tasks.OneSecondTimerTask;
import com.playtheatria.sessions.utils.PLog;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheatriaSessions extends JavaPlugin {
    private SessionManager sessionManager;
    private ServerSessionManager serverSessionManager;
    private SessionRepository sessionRepository;
    private ServerSessionRepository serverSessionRepository;
    private DatabaseTask databaseTask;
    private PLog log;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this);
        log = new PLog(configManager);
        Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials == null) {
            log.err("Essentials returned null, shutting down.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        TheatriaSessionsDB sessionsDB;
        // Ensure the data folder exists
        if (!getDataFolder().exists()) {
            boolean created = getDataFolder().mkdirs();
            if (created) {
                getLogger()
                        .log(
                                Level.INFO,
                                "Plugin data folder created at: {0}",
                                getDataFolder().getAbsolutePath());
            } else {
                getLogger()
                        .log(
                                Level.SEVERE,
                                "Failed to create plugin data folder: {0}",
                                getDataFolder().getAbsolutePath());
            }
        }
        try {
            sessionsDB = new TheatriaSessionsDB(getDataFolder(), log);
        } catch (IOException e) {
            log.err("Failed to create database: " + e.getMessage());
            log.err("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            sessionRepository = new SessionRepository(sessionsDB, log);
        } catch (SQLException e) {
            log.err("Failed to create sessionRepository: " + e.getMessage());
            log.err("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        switch (sessionRepository.loadSessions()) {
            case Ok<List<Session>, RepositoryException> ok -> {
                log.info("Loaded " + ok.value().size() + " sessions from the database.");
                sessionManager = new SessionManager(ok.value(), log);
            }
            case Err<List<Session>, RepositoryException> err -> {
                log.err("Failed to load sessions from the database: " + err.error().getMessage());
                log.err("Shutting down...");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }

        try {
            serverSessionRepository = new ServerSessionRepository(sessionsDB, log);
        } catch (SQLException e) {
            log.err("Failed to create ServerSessionRepository: " + e.getMessage());
            log.err("Shutting down...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        serverSessionManager =
                new ServerSessionManager(serverSessionRepository.loadServerSession());

        databaseTask =
                new DatabaseTask(
                        sessionRepository,
                        serverSessionRepository,
                        sessionManager,
                        serverSessionManager,
                        log);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(
                this,
                20 * configManager.getInitialBackupDuration(),
                20 * configManager.getBackupDuration());
        new OneSecondTimerTask(sessionManager, serverSessionManager, essentials)
                .runTaskTimer(this, 20, 20);
        Bukkit.getPluginManager()
                .registerEvents(
                        new DayChange(
                                sessionRepository,
                                serverSessionRepository,
                                sessionManager,
                                serverSessionManager,
                                log),
                        this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(sessionManager, log), this);
        Bukkit.getPluginManager().registerEvents(new RewardPlayer(configManager, log), this);
        Bukkit.getPluginManager()
                .registerEvents(new ServerSessionRewardCount(serverSessionManager, log), this);
        Bukkit.getPluginManager().registerEvents(new RewardCommunity(configManager, log), this);
        Objects.requireNonNull(getCommand("session"))
                .setExecutor(
                        new SessionCommand(sessionManager, serverSessionManager, configManager));
        Objects.requireNonNull(getCommand("community"))
                .setExecutor(new CommunityCommand(configManager, serverSessionManager));
        Objects.requireNonNull(getCommand("activity"))
                .setExecutor(new ActivityCommand(sessionManager));
        log.info("Loaded plugin.");
    }

    @Override
    public void onDisable() {
        if (databaseTask != null) databaseTask.cancel();
        if (sessionManager != null && sessionRepository != null) {
            for (Session session : sessionManager.getSessions().values()) {
                log.info(
                        "User: "
                                + session.getPlayerName()
                                + " had a session time of "
                                + session.getSessionTime());
                sessionRepository.createOrUpdate(session);
            }
        }
        if (serverSessionManager != null && serverSessionRepository != null) {
            ServerSession serverSession = serverSessionManager.getServerSession();
            log.info(
                    String.format(
                            "ServerSession Date: %s, RewardsEarned: %s, PlayersJoined: %s",
                            serverSession.getSessionDate(),
                            serverSession.getRewardsEarned(),
                            serverSession.getPlayersJoined()));
            serverSessionRepository.createOrUpdate(serverSession);
        }
    }
}
