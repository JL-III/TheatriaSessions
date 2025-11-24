package com.playtheatria.sessions.cache;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.utils.PLog;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SessionCache {
    private ConcurrentHashMap<UUID, Session> mappedSessions = new ConcurrentHashMap<>();
    private final PLog logger;

    public SessionCache(List<Session> sessions, PLog logger) {
        for (Session session : sessions) {
            mappedSessions.put(session.getPlayerUUID(), session);
        }
        this.logger = logger;
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
                                    "Failed to return a session from SessionCache"
                                            + " mappedSessions for UUID: %s",
                                    playerUUID)));
        }
        return new Ok<>(session);
    }

    public ConcurrentHashMap<UUID, Session> getSessions() {
        return mappedSessions;
    }

    public void createNewSession(@NotNull UUID playerUUID, @NotNull String playerName) {
        logger.debugFmt("Creating session for %s", playerName);
        mappedSessions.put(playerUUID, new Session(playerUUID, playerName));
    }

    public void addSession(@NotNull Session session) {
        mappedSessions.put(session.getPlayerUUID(), session);
    }

    public void resetSessions() {
        logger.debug("Purging sessions.");
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
        logger.debugFmt("Reset mapped sessions size: %s", mappedSessions.size());
    }
}
