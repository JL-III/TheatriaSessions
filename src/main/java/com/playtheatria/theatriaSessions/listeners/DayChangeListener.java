package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.tasks.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DayChangeListener implements Listener {
    private final SessionManager sessionManager;

    public DayChangeListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("Day change detected. Clearing sessions.")));
        sessionManager.resetSessions();
    }
}
