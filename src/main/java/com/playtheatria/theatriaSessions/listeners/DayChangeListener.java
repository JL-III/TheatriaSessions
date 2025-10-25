package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.ServerSession;
import com.playtheatria.theatriaSessions.database.repositories.ServerSessionRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
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

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        customLogger.sendDebug("[DayChangeEvent] Day change detected. Resetting sessions.");
        sessionManager.resetSessions();
        customLogger.sendDebug("[DayChangeEvent] Day change detected. Purging SessionRepository.");
        if (sessionRepository.purgeAll()) {
            customLogger.sendDebug("[DayChangeEvent] Purging SessionRepository succeeded.");
        } else {
            customLogger.sendDebug("[DayChangeEvent] Purging SessionRepository failed.");
        }

        ServerSession oldServerSession = serverSessionManager.getServerSession();
        customLogger.sendDebug(
                "[DayChangeEvent] Day change detected. Debug logs for oldServerSession:");
        customLogger.sendDebug(
                String.format(
                        "[DayChangeEvent] SessionDate: %s", oldServerSession.getSessionDate()));
        customLogger.sendDebug(
                String.format(
                        "[DayChangeEvent] RewardsEarned: %s", oldServerSession.getRewardsEarned()));
        customLogger.sendDebug(
                String.format(
                        "[DayChangeEvent] PlayersJoined: %s", oldServerSession.getPlayersJoined()));

        ServerSession serverSession = new ServerSession(LocalDate.now(TimeUtils.timeZone));
        customLogger.sendDebug("[DayChangeEvent] Day change detected. Setting new ServerSession");
        customLogger.sendDebug(
                String.format("[DayChangeEvent] SessionDate: %s", serverSession.getSessionDate()));
        customLogger.sendDebug(
                String.format(
                        "[DayChangeEvent] RewardsEarned: %s", serverSession.getRewardsEarned()));
        customLogger.sendDebug(
                String.format(
                        "[DayChangeEvent] PlayersJoined: %s", serverSession.getPlayersJoined()));
        serverSessionManager.setServerSession(serverSession);

        customLogger.sendDebug(
                "[DayChangeEvent] Day change detected. Purging ServerSessionRepository.");
        if (serverSessionRepository.purgeAll()) {
            customLogger.sendDebug("[DayChangeEvent] Purging ServerSessionRepository succeeded.");
        } else {
            customLogger.sendDebug("[DayChangeEvent] Purging ServerSessionRepository failed.");
        }
    }
}
