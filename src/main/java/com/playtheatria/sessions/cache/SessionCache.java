package com.playtheatria.sessions.cache;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.database.data.Session;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SessionCache {
    private ConcurrentHashMap<UUID, Session> mappedSessions = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger("TheatriaSessions " + SessionCache.class.getSimpleName());

    public SessionCache(List<Session> sessions) {
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
            return new Err<>(
                    new Exception(
                            String.format(
                                    "Failed to return a session from the SessionManager"
                                            + " mappedSessions for UUID: %s",
                                    playerUUID)));
        }
        return new Ok<>(mappedSessions.get(playerUUID));
    }

    public ConcurrentHashMap<UUID, Session> getSessions() {
        return mappedSessions;
    }

    public void createNewSession(@NotNull UUID playerUUID, @NotNull String playerName) {
        logger.log(Level.INFO, String.format("Creating session for %s", playerName));
        mappedSessions.put(playerUUID, new Session(playerUUID, playerName));
    }

    public void addSession(@NotNull Session session) {
        mappedSessions.put(session.getPlayerUUID(), session);
    }

    public void resetSessions() {
        logger.log(Level.INFO, "Purging sessions.");
        Set<UUID> onlinePlayers =
                Bukkit.getOnlinePlayers().stream()
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
                    updatedSessions.put(
                            session.getPlayerUUID(),
                            new Session(session.getPlayerUUID(), session.getPlayerName()));
                }
            }
        }

        mappedSessions = updatedSessions;
        logger.log(Level.INFO, "Reset mapped sessions size: {0}", mappedSessions.size());
    }
}
