package com.playtheatria.theatriaSessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.theatriaSessions.database.data.ResetTime;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.events.*;
import com.playtheatria.theatriaSessions.managers.ResetTimeManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

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
        for (Session session : sessionManager.getSessions().values()) {
            User user = essentials.getUser(session.getPlayerUUID());
            Player player = Bukkit.getPlayer(session.getPlayerUUID());
            if (user == null || player == null || player.isDead()) continue;
            if (!player.hasPermission("theatria.sessions.allow")) continue;
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
        ResetTime resetTime = resetTimeManager.getResetTime();
        LocalDateTime now = LocalDateTime.now(Util.timeZone);

        if (now.isAfter(resetTime.getNextResetHour())) {
            Bukkit.getPluginManager().callEvent(new HourChangeEvent(resetTime.getLastResetHour(), now));
            resetTimeManager.setResetTime(new ResetTime());
        }
    }
}
