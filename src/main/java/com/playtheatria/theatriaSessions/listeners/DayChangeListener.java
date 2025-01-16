package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChangeListener implements Listener {
    private final SessionRepository sessionRepository;
    private final SessionManager sessionManager;
    private final CustomLogger customLogger;

    public DayChangeListener(SessionRepository sessionRepository, SessionManager sessionManager, CustomLogger customLogger) {
        this.sessionRepository = sessionRepository;
        this.sessionManager = sessionManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        customLogger.sendDebug("[DayChangeEvent] Day change detected. Clearing sessions.");
        sessionManager.resetSessions();
        sessionRepository.purgeAll();
    }
}
