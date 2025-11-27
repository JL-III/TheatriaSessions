package com.playtheatria.sessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.events.RewardPlayerEvent;
import com.playtheatria.sessions.service.DailyStatsService;
import com.playtheatria.sessions.service.SessionService;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OneSecondTimer extends BukkitRunnable {
    private final DailyStatsService dailyStatsService;
    private final SessionService sessionService;
    private final Essentials essentials;
    private final ConfigManager cm;

    public OneSecondTimer(
            DailyStatsService dailyStatsService,
            SessionService sessionService,
            Essentials essentials,
            ConfigManager cm,
            PLog log) {
        this.dailyStatsService = dailyStatsService;
        this.sessionService = sessionService;
        this.essentials = essentials;
        this.cm = cm;
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

            int incrementValue = session.incrementSessionTime();
            int threshold = cm.getRewardThreshold();

            if (incrementValue == threshold / 2) {
                int remaining = threshold - incrementValue;
                int minutes = remaining / 60;
                player.sendMessage(
                        Component.text(String.format(cm.getNotifyMessage(), (minutes)))
                                .color(NamedTextColor.YELLOW)
                                .decorate(TextDecoration.ITALIC));
            }

            if (!session.hasEarnedReward(cm.getRewardThreshold()) || session.isRewarded()) continue;
            Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(player.getUniqueId()));
        }
        dailyStatsService.setPlayersJoined(sessionService.getSessionsCount());
    }
}
