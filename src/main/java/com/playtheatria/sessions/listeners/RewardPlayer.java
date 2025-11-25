package com.playtheatria.sessions.listeners;

import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.PLog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RewardPlayer implements Listener {
    private final SessionService sessionService;
    private final StreakService streakService;
    private final PLog log;

    public RewardPlayer(SessionService sessionService, StreakService streakService, PLog log) {
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.log = log;
    }

    @EventHandler
    public void onRewardPlayer(RewardPlayerEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerUUID());
        if (player == null || !player.isOnline()) {
            log.warn("Player reward returned offline or null");
            log.warn(String.format("Player UUID: %s", event.getPlayerUUID()));
            return;
        }

        sessionService.handleSession(event.getPlayerUUID(), player);
        streakService.handleStreak(event.getPlayerUUID(), player);
    }
}
