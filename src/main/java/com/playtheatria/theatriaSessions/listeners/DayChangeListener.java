package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.data.ResetTime;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.managers.ResetTimeManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChangeListener implements Listener {
    private final ResetTimeManager resetTimeManager;
    private final SessionManager sessionManager;

    public DayChangeListener(ResetTimeManager resetTimeManager, SessionManager sessionManager) {
        this.resetTimeManager = resetTimeManager;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        Util.sendFormattedLog("Day change detected. Clearing sessions.");
        sessionManager.resetSessions();
        resetTimeManager.setResetTime(new ResetTime());
    }
}
