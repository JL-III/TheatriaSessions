package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class SessionManagerDayChangeListener implements Listener {
    private final SessionManager sessionManager;

    public SessionManagerDayChangeListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        Util.sendFormattedLog("Day change detected by SessionManagerDayChangeListener. Clearing Sessions.");
        sessionManager.resetSessions();
    }
}
