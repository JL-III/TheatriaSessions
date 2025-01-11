package com.playtheatria.theatriaSessions.managers;

import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.result.Result;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SessionManager {
    private ConcurrentHashMap<UUID, Session> mappedSessions = new ConcurrentHashMap<>();

    public SessionManager(List<Session> sessions) {
        for (Session session : sessions) {
            mappedSessions.put(session.getPlayerUUID(), session);
        }
    }

    public boolean hasSession(@NotNull UUID playerUUID) {
        return mappedSessions.get(playerUUID) != null;
    }

    public Result<Session, Exception> getSession(@NotNull UUID playerUUID) {
        Session session = mappedSessions.get(playerUUID);
        if (session == null) {
            return new Err<>(new Exception(String.format("Failed to return a session from the SessionManager mappedSessions for UUID: %s", playerUUID)));
        }
        return new Ok<>(mappedSessions.get(playerUUID));
    }

    public ConcurrentHashMap<UUID, Session> getSessions() {
        return mappedSessions;
    }

    public void createNewSession(@NotNull UUID playerUUID, @NotNull String playerName) {
        mappedSessions.put(playerUUID, new Session(playerUUID, playerName));
    }

    public void addSession(@NotNull Session session) {
        mappedSessions.put(session.getPlayerUUID(), session);
    }

    public void resetSessions() {
        Set<UUID> onlinePlayers = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());

        ConcurrentHashMap<UUID, Session> updatedSessions = new ConcurrentHashMap<>();

        for (Session session : mappedSessions.values()) {
            if (onlinePlayers.contains(session.getPlayerUUID())) {
                if (!session.isRewarded()) {
                    // Player is online and has not reached the threshold
                    updatedSessions.put(session.getPlayerUUID(), session);
                } else {
                    // Player has reached the threshold, create a new session
                    updatedSessions.put(session.getPlayerUUID(), new Session(session.getPlayerUUID(), session.getPlayerName()));
                }
            }
        }

        mappedSessions = updatedSessions;
    }
}
