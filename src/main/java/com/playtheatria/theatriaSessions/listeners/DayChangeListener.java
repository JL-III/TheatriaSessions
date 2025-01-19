package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.events.time.DayChangeEvent;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChangeListener implements Listener {
    private final SessionRepository sessionRepository;
    private final SessionManager sessionManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public DayChangeListener(
            SessionRepository sessionRepository, SessionManager sessionManager,
            CustomLogger<TheatriaSessions, ConfigManager> customLogger
    ) {
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
