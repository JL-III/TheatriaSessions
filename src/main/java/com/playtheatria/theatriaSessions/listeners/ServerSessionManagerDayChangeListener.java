package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.data.ServerSession;
import com.playtheatria.theatriaSessions.enums.RewardTier;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDate;

public class ServerSessionManagerDayChangeListener implements Listener {
    private final ServerSessionManager serverSessionManager;

    public ServerSessionManagerDayChangeListener(ServerSessionManager serverSessionManager) {
        this.serverSessionManager = serverSessionManager;
    }

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        Util.sendFormattedLog("Day change detected by ServerSessionManagerDayChangeListener - Clearing ServerSessions.");
        serverSessionManager.setServerSession(new ServerSession(LocalDate.now()));
        for (RewardTier rewardTier : RewardTier.values()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp group default permission unsettemp " + rewardTier.getPermission());
        }
    }
}
