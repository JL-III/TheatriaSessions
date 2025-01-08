package com.playtheatria.theatriaSessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.theatriaSessions.data.ResetTime;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.managers.ResetTimeManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class OneSecondTimerTask extends BukkitRunnable {
    private final ResetTimeManager resetTimeManager;
    private final SessionManager sessionManager;
    private final Essentials essentials;

    public OneSecondTimerTask(ResetTimeManager resetTimeManager, SessionManager sessionManager, Essentials essentials) {
        this.resetTimeManager = resetTimeManager;
        this.sessionManager = sessionManager;
        this.essentials = essentials;
    }

    @Override
    public void run() {
        handleSessionIncrement();
        checkReset();
    }

    public void handleSessionIncrement() {
        for (Session session : sessionManager.getSessions()) {
            User user = essentials.getUser(session.getPlayerUUID());
            Player player = Bukkit.getPlayer(session.getPlayerUUID());
            if (user == null || player == null || player.isDead()) continue;
            if (user.isAfk()) {
                session.incrementAfkTime();
                continue;
            }

            session.incrementSessionTime();
            if (!session.hasEarnedReward() || session.isRewarded()) continue;
            Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
        }
    }

    public void checkReset() {
        LocalDateTime resetTime = resetTimeManager.getResetTime().getLastResetTime();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime nextScheduledReset = resetTimeManager.getResetTime().getNextResetTime();

        // Check if the current time is past the scheduled reset or more than 24 hours since last reset
        if (now.isAfter(nextScheduledReset) || Duration.between(resetTime, now).toHours() >= 24) {
            // Trigger the reset
            Bukkit.getPluginManager().callEvent(new DayChangeEvent());
        }
    }
}
