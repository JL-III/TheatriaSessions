package com.playtheatria.sessions.listeners;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.sessions.database.data.Streak;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.service.StreakService;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {
    private final SessionService sessionService;
    private final StreakService streakService;
    private final PLog log;

    public PlayerJoin(SessionService sessionService, StreakService streakService, PLog log) {
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.log = log;
    }

    /**
     * Handles PlayerJoinEvent to create a new session if one does not exist
     * @param event PlayerJoinEvent instance
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Check if player has permission to have a session
        if (!event.getPlayer().hasPermission(Util.PERMISSION_ALLOW)) return;

        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        if (!sessionService.hasSession(playerUUID)) {
            log.debug("No session found for " + playerName);
            sessionService.createNewSession(playerUUID, playerName);
        }
        if (!streakService.hasStreak(playerUUID)) {
            log.debug("No streak found for " + playerName);
            streakService.createNewStreak(playerUUID, playerName);
        } else {
            switch (streakService.getStreak(playerUUID)) {
                case Ok<Streak, Exception> okStreak -> {
                    Streak streak = okStreak.value();
                    LocalDate lastEarnedDate = streak.getLastEarnedDate();
                    if (lastEarnedDate == null) {
                        log.debug(
                                String.format(
                                        "No last earned date for %s, skipping streak check.",
                                        playerName));
                        return;
                    }
                    LocalDate today = LocalDate.now(TimeUtils.timeZone);
                    long daysBetween = ChronoUnit.DAYS.between(lastEarnedDate, today);
                    if (daysBetween > 1) {
                        log.debug(
                                String.format(
                                        "Resetting streak for %s. Days since last earned: %d",
                                        playerName, daysBetween));
                        // Reset streak
                        streakService.resetCurrentStreak(streak);
                    }
                }
                case Err<Streak, Exception> err -> {
                    log.err(
                            String.format(
                                    "Failed to retrieve streak for %s on login: %s",
                                    playerName, err.error()));
                }
            }
        }
    }
}
