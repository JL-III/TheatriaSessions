package com.playtheatria.theatriaSessions.tasks;

import com.playtheatria.theatriaSessions.data.Session;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SessionManager {
    private List<Session> sessions;

    public SessionManager(List<Session> sessions) {
        this.sessions = sessions;
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
