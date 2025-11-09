package com.playtheatria.sessions.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.managers.DailyStatsCache;
import com.playtheatria.sessions.managers.SessionCache;
import com.playtheatria.sessions.utils.Util;

public class OneSecondTimerTask extends BukkitRunnable {
    private final SessionCache sessionCache;
    private final DailyStatsCache dailyStatsCache;
    private final Essentials essentials;

    public OneSecondTimerTask(
            SessionCache sessionCache, DailyStatsCache dailyStatsCache, Essentials essentials) {
        this.sessionCache = sessionCache;
        this.dailyStatsCache = dailyStatsCache;
        this.essentials = essentials;
    }

    @Override
    public void run() {
        for (Session session : sessionCache.getSessions().values()) {
            User user = essentials.getUser(session.getPlayerUUID());
            Player player = Bukkit.getPlayer(session.getPlayerUUID());
            if (user == null || player == null || player.isDead()) continue;
            if (!player.hasPermission(Util.PERMISSION_ALLOW)) continue;
            if (user.isAfk()) {
                session.incrementAfkTime();
                continue;
            }

            session.incrementSessionTime();
            if (!session.hasEarnedReward() || session.isRewarded()) continue;
            Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
        }
        dailyStatsCache.getDayStats().setPlayersJoined(sessionCache.getSessions().size());
    }
}
