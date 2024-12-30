package com.playtheatria.theatriaSessions.tasks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SessionTracker extends BukkitRunnable {
    private final TheatriaSessions theatriaSessions;
    private final Essentials essentials;
    private LocalDate currentDate;
    private List<Session> sessions = new ArrayList<>();

    public SessionTracker(TheatriaSessions theatriaSessions, Essentials essentials) {
        this.theatriaSessions = theatriaSessions;
        this.essentials = essentials;
        this.currentDate = LocalDate.now();
    }

    @Override
    public void run() {
        if (currentDate.isBefore(LocalDate.now())) {
            handleDayChange();
            return;
        }
        for (Session session : sessions) {
            User user = essentials.getUser(session.getPlayerUUID());
            Player player = Bukkit.getPlayer(session.getPlayerUUID());
            if (user == null || user.isAfk() || player == null) continue;

            session.incrementSessionTime();
            if (!session.hasEarnedReward() || session.isRewarded()) continue;
            rewardPlayer(session);
        }
    }

    public void rewardPlayer(Session session) {
        Player player = Bukkit.getPlayer(session.getPlayerUUID());
        if (player == null || !player.isOnline()) {
            Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("Tried to reward a player but the player in the session returned as offline or null.")));
            return;
        }

        session.setRewarded();
        player.sendMessage("You achieved the session requirement for today!");
        Bukkit.getConsoleSender().sendMessage(
                "Reward given to player: " + player.getName()
                        + "ItemsDropped: " + player.getInventory().addItem(new ItemStack(Material.NETHER_STAR, 10))
        );
    }

    public void handleDayChange() {
        currentDate = LocalDate.now();
        Bukkit.getConsoleSender().sendMessage(Util.formatLog(Component.text("Day changed to " + currentDate)));
        theatriaSessions.getServer().getPluginManager().callEvent(new DayChangeEvent());
    }

    public boolean hasSession(UUID playerUUID) {
        for (Session session : sessions) {
            if (session.getPlayerUUID().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public List<Session> getSessions() {
        return this.sessions;
    }

    public void addSession(UUID playerUUID, String playerName) {
        sessions.add(new Session(playerUUID, playerName));
    }

    public void resetSessions() {
        Set<UUID> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());

        List<Session> updatedSessions = new ArrayList<>();

        for (Session session : sessions) {
            if (onlinePlayers.contains(session.getPlayerUUID())) {
                if (!session.isRewarded()) {
                    // Player is online and has not reached the threshold
                    updatedSessions.add(session);
                } else {
                    // Player has reached the threshold, create a new session
                    updatedSessions.add(new Session(session.getPlayerUUID(), session.getPlayerName()));
                }
            }
        }

        sessions = updatedSessions;
    }
}
