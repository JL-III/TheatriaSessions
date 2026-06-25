package com.playtheatria.sessions;

import com.earth2me.essentials.Essentials;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.api.SessionsAPI;
import com.playtheatria.sessions.cache.DailyStatsCache;
import com.playtheatria.sessions.cache.SessionCache;
import com.playtheatria.sessions.cache.StreakCache;
import com.playtheatria.sessions.commands.SessionCommand;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.TheatriaSessionsDB;
import com.playtheatria.sessions.database.data.DailyStats;
import com.playtheatria.sessions.database.repositories.DailyStatsRepo;
import com.playtheatria.sessions.database.repositories.SessionRepo;
import com.playtheatria.sessions.database.repositories.StreakRepo;
import com.playtheatria.sessions.listeners.CommunityBossBar;
import com.playtheatria.sessions.listeners.DailyStatsRewardCount;
import com.playtheatria.sessions.listeners.DayChange;
import com.playtheatria.sessions.listeners.PlayerJoin;
import com.playtheatria.sessions.listeners.RewardCommunity;
import com.playtheatria.sessions.listeners.RewardPlayer;
import com.playtheatria.sessions.records.CommandRecord;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.tasks.DatabaseTask;
import com.playtheatria.sessions.tasks.OneSecondTimer;
import com.playtheatria.sessions.utils.PLog;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheatriaSessions extends JavaPlugin {
    private DailyStatsCache dailyStatsCache;
    private DailyStatsRepo dailyStatsRepo;
    private DailyStatsService dailyStatsService;

    private SessionCache sessionCache;
    private SessionRepo sessionRepo;
    private SessionService sessionService;

    private StreakCache streakCache;
    private StreakRepo streakRepo;
    private StreakService streakService;

    private DatabaseTask databaseTask;
    private OneSecondTimer oneSecondTimer;
    private CommunityBossBar communityBossBar;
    private Essentials essentials;
    private TheatriaSessionsDB sessionsDB;
    private DailyStats dailyStats;
    private PLog log;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigManager cm = new ConfigManager(this);
        log = new PLog(cm);

        if (!ok(essentials(), v -> essentials = v)) return;
        if (!ok(dataFolder(), v -> log.info(v))) return;
        if (!ok(sessionsDB(), v -> sessionsDB = v)) return;
        if (!ok(sessionRepo(), v -> sessionRepo = v)) return;
        if (!ok(dailyStatsRepo(), v -> dailyStatsRepo = v)) return;
        if (!ok(streakRepo(), v -> streakRepo = v)) return;
        if (!ok(sessionRepo.load(), v -> sessionCache = new SessionCache(v, log))) return;
        if (!ok(dailyStatsRepo.load(), v -> dailyStats = v)) return;
        if (!ok(streakRepo.load(), v -> streakCache = new StreakCache(v, log))) return;

        sessionService = new SessionService(sessionCache, sessionRepo, cm, log);
        streakService = new StreakService(streakCache, streakRepo, cm, log);
        dailyStatsCache = new DailyStatsCache(dailyStats);
        dailyStatsService = new DailyStatsService(dailyStatsCache, dailyStatsRepo, log);

        // Publish the public query API so other plugins (e.g. TheatriaOnboarding)
        // can read daily-reward progress instead of inferring it from vanilla state.
        SessionsAPI.register(sessionService, cm, log);

        databaseTask = new DatabaseTask(dailyStatsService, sessionService, streakService, log);
        oneSecondTimer = new OneSecondTimer(dailyStatsService, sessionService, essentials, cm, log);

        log.debug(
                String.format(
                        "[onEnable] Running on thread: %s", Thread.currentThread().getName()));
        oneSecondTimer.runTaskTimer(this, 20, 20);
        databaseTask.runTaskTimerAsynchronously(
                this, 20 * cm.getInitDelay(), 20 * cm.getBackupDuration());

        registerEvents(cm, dailyStatsService, sessionService, streakService, log);
        // Re-attach the community boss bar for anyone already online (e.g. after a /reload)
        // if a bonus is currently active.
        communityBossBar.refresh();

        registerCommands(
                List.of(
                        new CommandRecord(
                                "session",
                                new SessionCommand(
                                        dailyStatsService,
                                        sessionService,
                                        streakService,
                                        cm,
                                        communityBossBar))));
        log.info("Loaded plugin.");
    }

    @Override
    public void onDisable() {
        SessionsAPI.unregister();
        if (databaseTask != null) databaseTask.cancel();
        if (oneSecondTimer != null) oneSecondTimer.cancel();
        if (communityBossBar != null) communityBossBar.hideFromAll();
        sessionService.persist(true);
        dailyStatsService.persist(true);
        streakService.persist(true);
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
            DailyStatsService dss,
            SessionService ss,
            StreakService sts,
            PLog log) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new DayChange(dss, ss, cm, log), this);
        pm.registerEvents(new PlayerJoin(ss, sts, log), this);
        pm.registerEvents(new RewardPlayer(this, ss, sts, cm, log), this);
        pm.registerEvents(new DailyStatsRewardCount(dss, log), this);
        pm.registerEvents(new RewardCommunity(cm, log), this);
        communityBossBar = new CommunityBossBar(this, cm, dss, log);
        pm.registerEvents(communityBossBar, this);
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
    private Result<SessionRepo, Exception> sessionRepo() {
        try {
            return new Ok<>(new SessionRepo(sessionsDB, log));
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

    private Result<StreakRepo, Exception> streakRepo() {
        try {
            return new Ok<>(new StreakRepo(sessionsDB, log));
        } catch (SQLException e) {
            return new Err<>(new SQLException("Failed to create StreakRepo", e));
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
