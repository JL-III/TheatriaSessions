package com.playtheatria.sessions.listeners;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.database.data.ServerSession;
import com.playtheatria.sessions.database.repositories.ServerSessionRepository;
import com.playtheatria.sessions.database.repositories.SessionRepository;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.managers.ServerSessionManager;
import com.playtheatria.sessions.managers.SessionManager;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.theatriaTime.events.DayChangeEvent;
import java.time.LocalDate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChange implements Listener {
    private final SessionRepository sessionRepository;
    private final ServerSessionRepository serverSessionRepository;
    private final ServerSessionManager serverSessionManager;
    private final SessionManager sessionManager;
    private final PLog log;

    public DayChange(
            SessionRepository sessionRepository,
            ServerSessionRepository serverSessionRepository,
            SessionManager sessionManager,
            ServerSessionManager serverSessionManager,
            PLog log) {
        this.sessionRepository = sessionRepository;
        this.serverSessionRepository = serverSessionRepository;
        this.serverSessionManager = serverSessionManager;
        this.sessionManager = sessionManager;
        this.log = log;
    }

    /**
     * Handles DayChangeEvent to reset sessions and server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetSessions(DayChangeEvent event) {
        log.debug("[DayChangeEvent] Resetting sessions.");
        sessionManager.resetSessions();
        switch (sessionRepository.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> log.debug(
                    String.format("Deleted %d" + " entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> log.debug(
                    String.format("Purging SessionRepository failed %s", err.error().getMessage()));
        }
    }

    /**
     * Handles DayChangeEvent to reset server session
     * @param event DayChangeEvent instance
     */
    @EventHandler
    public void onDayChangeResetServerSession(DayChangeEvent event) {
        log.debug("[DayChangeEvent] resetting ServerSession.");
        log.debug("Logs for oldServerSession");
        printServerSessionDebugLogs(serverSessionManager.getServerSession());

        log.debug(" Setting new ServerSession");
        serverSessionManager.setServerSession(new ServerSession(LocalDate.now(TimeUtils.timeZone)));
        printServerSessionDebugLogs(serverSessionManager.getServerSession());

        log.debug("Purging ServerSessionRepository.");
        switch (serverSessionRepository.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> log.debug(
                    String.format("Deleted" + " %d entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> log.debug(
                    String.format("Purging ServerSession failed %s", err.error().getMessage()));
        }
    }

    /**
     * Prints debug logs for the provided ServerSession
     * @param serverSession ServerSession to print debug logs for
     */
    private void printServerSessionDebugLogs(ServerSession serverSession) {
        log.debug(String.format("SessionDate %s", serverSession.getSessionDate()));
        log.debug(String.format("RewardsEarned %s", serverSession.getRewardsEarned()));
        log.debug(String.format("PlayersJoined %s", serverSession.getPlayersJoined()));
    }
}
