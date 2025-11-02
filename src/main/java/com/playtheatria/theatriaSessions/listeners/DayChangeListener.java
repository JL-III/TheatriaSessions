package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.ServerSession;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.errors.RepositoryException;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import java.time.LocalDate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChangeListener implements Listener {
    private final SessionRepository sessionRepository;
    private final ServerSessionRepository serverSessionRepository;
    private final ServerSessionManager serverSessionManager;
    private final SessionManager sessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public DayChangeListener(
            SessionRepository sessionRepository,
            ServerSessionRepository serverSessionRepository,
            SessionManager sessionManager,
            ServerSessionManager serverSessionManager,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.sessionRepository = sessionRepository;
        this.serverSessionRepository = serverSessionRepository;
        this.serverSessionManager = serverSessionManager;
        this.sessionManager = sessionManager;
        this.customLogger = customLogger;
    }

    /**
     * Handles DayChangeEvent to reset sessions and server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetSessions(DayChangeEvent event) {
        customLogger.sendDebug("[DayChangeEvent] Resetting sessions.");
        sessionManager.resetSessions();
        switch (sessionRepository.purgeAll()) {
            case Ok<Integer, RepositoryException> ok -> customLogger.sendDebug(
                    String.format("Deleted %d" + " entries.", ok.value()));
            case Err<Integer, RepositoryException> err -> customLogger.sendDebug(
                    String.format("Purging SessionRepository failed %s", err.error().getMessage()));
        }
    }

    /**
     * Handles DayChangeEvent to reset server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetServerSession(DayChangeEvent event) {
        customLogger.sendDebug("[DayChangeEvent] resetting ServerSession.");
        customLogger.sendDebug("Logs for oldServerSession");
        printServerSessionDebugLogs(serverSessionManager.getServerSession());

        customLogger.sendDebug(" Setting new ServerSession");
        serverSessionManager.setServerSession(new ServerSession(LocalDate.now(TimeUtils.timeZone)));
        printServerSessionDebugLogs(serverSessionManager.getServerSession());

        customLogger.sendDebug("Purging ServerSessionRepository.");
        switch (serverSessionRepository.purgeAll()) {
            case Ok<Integer, RepositoryException> ok -> customLogger.sendDebug(
                    String.format("Deleted" + " %d entries.", ok.value()));
            case Err<Integer, RepositoryException> err -> customLogger.sendDebug(
                    String.format("Purging ServerSession failed %s", err.error().getMessage()));
        }
    }

    /**
     * Prints debug logs for the provided ServerSession
     * @param serverSession ServerSession to print debug logs for
     */
    private void printServerSessionDebugLogs(ServerSession serverSession) {
        customLogger.sendDebug(String.format("SessionDate %s", serverSession.getSessionDate()));
        customLogger.sendDebug(String.format("RewardsEarned %s", serverSession.getRewardsEarned()));
        customLogger.sendDebug(String.format("PlayersJoined %s", serverSession.getPlayersJoined()));
    }
}
