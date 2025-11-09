package com.playtheatria.sessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OneSecondTimerTask extends BukkitRunnable {
    private final DailyStatsService dailyStatsService;
    private final SessionService sessionService;
    private final Essentials essentials;

    public OneSecondTimerTask(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            Essentials essentials) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.essentials = essentials;
    }

    @Override
    public void run() {
        for (Session session : sessionService.getSessions()) {
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
        dailyStatsService.setPlayersJoined(sessionService.getSessionsCount());
    }
}
