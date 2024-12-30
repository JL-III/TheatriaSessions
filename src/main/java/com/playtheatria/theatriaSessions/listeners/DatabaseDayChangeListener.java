package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DatabaseDayChangeListener implements Listener {
    private final SessionRepository sessionRepository;

    public DatabaseDayChangeListener(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        if (!sessionRepository.purgeAll()) {
            Util.sendFormattedLog("Error purging all sessions from database.");
        }
    }
}
