package com.playtheatria.theatriaSessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.managers.ServerSessionManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OneSecondTimerTask extends BukkitRunnable {
    private final SessionManager sessionManager;
    private final ServerSessionManager serverSessionManager;
    private final Essentials essentials;

    public OneSecondTimerTask(
            SessionManager sessionManager,
            ServerSessionManager serverSessionManager,
            Essentials essentials) {
        this.sessionManager = sessionManager;
        this.serverSessionManager = serverSessionManager;
        this.essentials = essentials;
    }

    @Override
    public void run() {
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
        serverSessionManager
                .getServerSession()
                .setPlayersJoined(sessionManager.getSessions().size());
    }
}
