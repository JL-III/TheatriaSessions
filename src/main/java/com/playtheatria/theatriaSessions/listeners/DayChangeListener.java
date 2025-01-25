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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDate;

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
            CustomLogger<TheatriaSessions, ConfigManager> customLogger
    ) {
        this.sessionRepository = sessionRepository;
        this.serverSessionRepository = serverSessionRepository;
        this.serverSessionManager = serverSessionManager;
        this.sessionManager = sessionManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        customLogger.sendDebug("[DayChangeEvent] Day change detected. Clearing sessions.");
        sessionManager.resetSessions();
        sessionRepository.purgeAll();

        ServerSession serverSession = new ServerSession(LocalDate.now(TimeUtils.timeZone));
        serverSessionManager.setServerSession(serverSession);
        serverSessionRepository.purgeAll();
    }
}
