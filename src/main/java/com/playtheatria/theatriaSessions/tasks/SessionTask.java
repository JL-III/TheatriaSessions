package com.playtheatria.theatriaSessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.events.RewardPlayerEvent;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;

public class SessionTask extends BukkitRunnable {
    private final SessionManager sessionManager;
    private final Essentials essentials;
    private LocalDate currentDate;

    public SessionTask(SessionManager sessionManager, Essentials essentials) {
        this.sessionManager = sessionManager;
        this.essentials = essentials;
        this.currentDate = LocalDate.now();
    }

    @Override
    public void run() {
        if (currentDate.isBefore(LocalDate.now())) {
            currentDate = LocalDate.now();
            Bukkit.getPluginManager().callEvent(new DayChangeEvent());
            return;
        }
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
            player.sendActionBar(Component.text("You have a reward! Do /daily-reward to redeem!").color(NamedTextColor.RED));
//            Bukkit.getPluginManager().callEvent(new RewardPlayerEvent(session));
        }
    }
}
