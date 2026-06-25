package com.playtheatria.sessions.api;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.PLog;
import java.util.UUID;

/**
 * Stable, public entry point other plugins use to query a player's daily-reward
 * progress without reaching into TheatriaSessions' internals.
 *
 * <p>This is the supported integration surface -- e.g. TheatriaOnboarding's DAILY
 * task asks here instead of guessing from the vanilla {@code PLAY_ONE_MINUTE}
 * statistic. Every method takes and returns only JDK types ({@link UUID},
 * {@code boolean}, {@code int}), so a consumer can bind to it reflectively with no
 * compile-time dependency on this plugin (and nothing here leaks a shaded type):
 *
 * <pre>{@code
 * Class<?> apiClass = Class.forName("com.playtheatria.sessions.api.SessionsAPI");
 * Object api = apiClass.getMethod("get").invoke(null); // null when not enabled
 * if (api != null) {
 *     boolean earned = (boolean) apiClass
 *             .getMethod("hasEarnedDailyReward", UUID.class)
 *             .invoke(api, playerUuid);
 * }
 * }</pre>
 *
 * <p>Obtain the live instance via {@link #get()}. It exists only while the plugin
 * is enabled: {@code get()} returns {@code null} before {@code onEnable} and after
 * {@code onDisable}, so reflective callers should treat null as "unavailable" and
 * fall back to their own detection.
 *
 * <p>A player only has a tracked session when they are online and hold the
 * {@code theatria.sessions.allow} permission (granted by default; removed only to
 * exclude alts). The query methods return {@code false}/{@code 0} for anyone
 * without a current session -- offline, not yet joined today, or excluded -- which
 * is the intended answer for those cases.
 */
public final class SessionsAPI {

    private static volatile SessionsAPI instance;

    private final SessionService sessionService;
    private final ConfigManager configManager;
    private final PLog log;

    private SessionsAPI(SessionService sessionService, ConfigManager configManager, PLog log) {
        this.sessionService = sessionService;
        this.configManager = configManager;
        this.log = log;
    }

    /**
     * Installs the singleton. Called once from {@code TheatriaSessions#onEnable}
     * after the session service and config are ready.
     */
    public static void register(SessionService sessionService, ConfigManager configManager, PLog log) {
        instance = new SessionsAPI(sessionService, configManager, log);
    }

    /** Clears the singleton on plugin disable. */
    public static void unregister() {
        instance = null;
    }

    /**
     * The live API instance, or {@code null} when TheatriaSessions is not currently
     * enabled. Reflective consumers should treat null as "unavailable".
     */
    public static SessionsAPI get() {
        return instance;
    }

    /**
     * Whether the player has actually earned (been granted) today's daily reward.
     * This flips true only after the reward is dispatched and clears at the daily
     * session reset, making it the precise analogue of "earned their daily reward".
     * Returns {@code false} when the player has no active session.
     */
    public boolean hasEarnedDailyReward(UUID playerUUID) {
        return switch (sessionService.getSession(playerUUID)) {
            case Ok<Session, Exception> ok -> {
                Session session = ok.value();
                log.debugFmt(
                        "[SessionsAPI] hasEarnedDailyReward(%s) -> %b (%ds/%ds active)",
                        new Object[] {
                            playerUUID, session.isRewarded(), session.getSessionTime(),
                            configManager.getRewardThreshold()
                        });
                yield session.isRewarded();
            }
            case Err<Session, Exception> ignored -> {
                log.debugFmt("[SessionsAPI] hasEarnedDailyReward(%s) -> false (no session)", playerUUID);
                yield false;
            }
        };
    }

    /**
     * Whether the player's active (non-AFK) playtime today has reached the reward
     * threshold. Unlike {@link #hasEarnedDailyReward(UUID)} this can read true in
     * the brief window after the bar is crossed but before the reward is dispatched.
     * Returns {@code false} when the player has no active session.
     */
    public boolean hasMetThreshold(UUID playerUUID) {
        return switch (sessionService.getSession(playerUUID)) {
            case Ok<Session, Exception> ok -> ok.value()
                    .hasEarnedReward(configManager.getRewardThreshold());
            case Err<Session, Exception> ignored -> false;
        };
    }

    /**
     * The player's accumulated active (non-AFK) session seconds today, or {@code 0}
     * if they have no active session. Pair with {@link #getThresholdSeconds()} to
     * show real progress towards the daily reward.
     */
    public int getSessionSeconds(UUID playerUUID) {
        return switch (sessionService.getSession(playerUUID)) {
            case Ok<Session, Exception> ok -> ok.value().getSessionTime();
            case Err<Session, Exception> ignored -> 0;
        };
    }

    /** The active-playtime threshold, in seconds, required to earn the daily reward. */
    public int getThresholdSeconds() {
        return configManager.getRewardThreshold();
    }

    /** Whether the player currently has a tracked session for today. */
    public boolean hasSession(UUID playerUUID) {
        return sessionService.hasSession(playerUUID);
    }
}
