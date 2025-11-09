package com.playtheatria.sessions;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.commands.ActivityCommand;
import com.playtheatria.sessions.commands.CommunityCommand;
import com.playtheatria.sessions.commands.SessionCommand;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.DailyStatsRepo;
import com.playtheatria.sessions.database.repositories.SessionRepository;
import com.playtheatria.sessions.listeners.DailyStatsRewardCount;
import com.playtheatria.sessions.listeners.DayChange;
import com.playtheatria.sessions.listeners.PlayerJoin;
import com.playtheatria.sessions.listeners.RewardCommunity;
import com.playtheatria.sessions.listeners.RewardPlayer;
import com.playtheatria.sessions.managers.DailyStatsCache;
import com.playtheatria.sessions.managers.SessionCache;
import com.playtheatria.sessions.records.CommandRecord;
import com.playtheatria.sessions.tasks.DatabaseTask;
import com.playtheatria.sessions.tasks.OneSecondTimerTask;
import com.playtheatria.sessions.utils.PLog;

public final class TheatriaSessions extends JavaPlugin {
    private SessionCache sessionManager;
    private DailyStatsCache dailyStatsCache;
    private DailyStatsRepo dailyStatsRepo;
    private SessionRepository sessionRepo;
    private DatabaseTask databaseTask;
    private PLog log;
    private Essentials essentials;
    private TheatriaSessionsDB sessionsDB;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this);
        log = new PLog(configManager);

        if (!ok(essentials(), v -> essentials = v)) return;
        if (!ok(dataFolder(), v -> log.info(v))) return;
        if (!ok(sessionsDB(), v -> sessionsDB = v)) return;
        if (!ok(sessionRepo(), v -> sessionRepo = v)) return;
        if (!ok(
                sessionRepo.load(),
                v -> {
                    log.info("Loaded " + v.size() + " sessions from the database.");
                    sessionManager = new SessionCache(v, log);
                })) {
            return;
        }
        if (!ok(
                dailyStatsRepo(),
                v -> {
                    dailyStatsRepo = v;
                })) {
            return;
        }

        dailyStatsCache = new DailyStatsCache(dailyStatsRepo.load());

        databaseTask =
                new DatabaseTask(sessionRepo, dailyStatsRepo, sessionManager, dailyStatsCache, log);
        // start first backup after ~10 minutes, continue every ~10 minutes
        databaseTask.runTaskTimer(
                this,
                20 * configManager.getInitialBackupDuration(),
                20 * configManager.getBackupDuration());
        new OneSecondTimerTask(sessionManager, dailyStatsCache, essentials)
                .runTaskTimer(this, 20, 20);

        registerEvents(
                configManager, sessionManager, sessionRepo, dailyStatsCache, dailyStatsRepo, log);

        registerCommands(
                List.of(
                        new CommandRecord(
                                "session",
                                new SessionCommand(sessionManager, dailyStatsCache, configManager)),
                        new CommandRecord(
                                "community", new CommunityCommand(configManager, dailyStatsCache)),
                        new CommandRecord("activity", new ActivityCommand(sessionManager))));
        log.info("Loaded plugin.");
    }

    @Override
    public void onDisable() {
        if (databaseTask != null) databaseTask.cancel();
        if (sessionManager != null && sessionRepo != null) {
            for (Session session : sessionManager.getSessions().values()) {
                log.info(
                        "User: "
                                + session.getPlayerName()
                                + " had a session time of "
                                + session.getSessionTime());
                sessionRepo.createOrUpdate(session);
            }
        }
        if (dailyStatsCache != null && dailyStatsRepo != null) {
            DailyStats currDayStats = dailyStatsCache.getDayStats();
            log.info(
                    String.format(
                            "Date: %s, RewardsEarned: %s, PlayersJoined: %s",
                            currDayStats.getDate(),
                            currDayStats.getRewardsEarned(),
                            currDayStats.getPlayersJoined()));
            dailyStatsRepo.createOrUpdate(currDayStats);
        }
    }

    private void registerCommands(List<CommandRecord> records) {
        for (CommandRecord record : records) {
            PluginCommand cmd = getCommand(record.name());
            if (cmd != null) {
                cmd.setExecutor(record.executor());
            } else {
                log.err("Failed to register command: " + record.name());
                log.err("Shutting down plugin.");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        }
    }

    private void registerEvents(
            ConfigManager cm,
            SessionCache sm,
            SessionRepository sr,
            DailyStatsCache currDayStatsCache,
            DailyStatsRepo currDayStatsRepo,
            PLog log) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new DayChange(sr, currDayStatsRepo, sm, currDayStatsCache, log), this);
        pm.registerEvents(new PlayerJoin(sm, log), this);
        pm.registerEvents(new RewardPlayer(cm, log), this);
        pm.registerEvents(new DailyStatsRewardCount(currDayStatsCache, log), this);
        pm.registerEvents(new RewardCommunity(cm, log), this);
    }

    /**
     * Attempts to get the Essentials plugin
     * @return Result containing Essentials instance if found, or an Exception if not found
     */
    private Result<Essentials, Exception> essentials() {
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (ess != null) {
            return new Ok<>(ess);
        } else {
            return new Err<>(new Exception("Essentials plugin not found or not loaded."));
        }
    }

    /**
     * Ensures the plugin data folder exists
     * @return Result indicating success or failure of folder creation
     */
    private Result<String, Exception> dataFolder() {
        File dir = getDataFolder();
        String path = dir.getAbsolutePath();
        if (dir.exists())
            return new Ok<>(String.format("Plugin data folder already exists at: %s", path));

        if (dir.mkdirs()) return new Ok<>(String.format("Plugin data folder created at: %s", path));

        return new Err<>(
                new IOException(String.format("Failed to create plugin data folder: %s", path)));
    }

    /**
     * Attempts to create or open the TheatriaSessionsDB
     * @return Result containing TheatriaSessionsDB if successful, or an Exception if failed
     */
    private Result<TheatriaSessionsDB, Exception> sessionsDB() {
        try {
            return new Ok<>(new TheatriaSessionsDB(getDataFolder(), log));
        } catch (IOException e) {
            return new Err<>(new SQLException("Failed to get new session database", e));
        }
    }

    /**
     * Utility method to get a SessionRepository
     * @return Result containing SessionRepository if successful, or an Exception if failed
     */
    private Result<SessionRepository, Exception> sessionRepo() {
        try {
            return new Ok<>(new SessionRepository(sessionsDB, log));
        } catch (SQLException e) {
            return new Err<>(new SQLException("Failed to get new session repository", e));
        }
    }

    private Result<DailyStatsRepo, Exception> dailyStatsRepo() {
        try {
            return new Ok<>(new DailyStatsRepo(sessionsDB, log));
        } catch (SQLException e) {
            return new Err<>(new SQLException("Failed to create CurrentDayStatsRepo", e));
        }
    }

    /**
     * Utility method to require an Ok result, or disable the plugin on Err
     * @param r The Result to check
     * @param onSuccess The Consumer to execute if the Result is Ok
     * @param <T> The type of the Ok value
     * @return true if Ok, false if Err (plugin disabled)
     */
    private <T, E extends Exception> boolean ok(Result<T, E> r, Consumer<T> onSuccess) {

        switch (r) {
            case Ok<T, E> ok -> {
                onSuccess.accept(ok.value());
                return true;
            }
            case Err<T, E> err -> {
                log.err("Shutting down. Reason: " + err.error().getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            }
        }
    }
}
