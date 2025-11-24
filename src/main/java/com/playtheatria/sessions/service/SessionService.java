package com.playtheatria.sessions.service;

import com.j256.ormlite.dao.Dao;
import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.result.Result;
import com.playtheatria.sessions.cache.SessionCache;
import com.playtheatria.sessions.config.ConfigManager;
import com.playtheatria.sessions.database.data.Session;
import com.playtheatria.sessions.database.repositories.SessionRepo;
import com.playtheatria.sessions.errors.PersistenceException;
import com.playtheatria.sessions.events.IncrementRewardCountEvent;
import com.playtheatria.sessions.utils.PLog;
import com.playtheatria.sessions.utils.Util;
import java.util.Collection;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SessionService {
    private final SessionCache cache;
    private final SessionRepo repo;
    private final ConfigManager cm;
    private final PLog log;

    public SessionService(SessionCache cache, SessionRepo repo, ConfigManager cm, PLog log) {
        this.cache = cache;
        this.repo = repo;
        this.cm = cm;
        this.log = log;
    }

    public void reset() {
        cache.resetSessions();
        switch (repo.purgeAll()) {
            case Ok<Integer, PersistenceException> ok -> log.debug(
                    String.format("Deleted %d" + " entries.", ok.value()));
            case Err<Integer, PersistenceException> err -> log.err(
                    String.format("Purging SessionRepository failed %s", err.error().getMessage()));
        }
    }

    public Result<Session, Exception> getSession(UUID playerUUID) {
        return cache.getSession(playerUUID);
    }

    public boolean hasSession(UUID playerUUID) {
        return cache.hasSession(playerUUID);
    }

    public void createNewSession(UUID playerUUID, String playerName) {
        cache.createNewSession(playerUUID, playerName);
    }

    public Collection<Session> getSessions() {
        return cache.getSessions().values();
    }

    public int getSessionsCount() {
        return cache.getSessions().size();
    }

    public void addSession(Session session) {
        cache.addSession(session);
    }

    public void persist(boolean verbose) {
        log.debugFmt("[persist] Running on thread: %s", Thread.currentThread().getName());
        for (Session session : getSessions()) {
            switch (repo.createOrUpdate(session)) {
                case Ok<Dao.CreateOrUpdateStatus, PersistenceException> ok -> {
                    Dao.CreateOrUpdateStatus status = ok.value();
                    String msg =
                            String.format(
                                    "Session persisted successfully | created: %s, updated: %s,"
                                            + " lines updated: %s",
                                    status.isCreated(),
                                    status.isUpdated(),
                                    status.getNumLinesChanged());
                    if (verbose) {
                        log.info(msg + session);
                    } else {
                        log.debug(msg + session);
                    }
                }
                case Err<Dao.CreateOrUpdateStatus, PersistenceException> err -> {
                    log.errFmt("Error persisting session: %s", err.error().getMessage() + session);
                }
            }
        }
    }

    public void handleSession(UUID playerUUID, Player player) {
        switch (getSession(playerUUID)) {
            case Ok<Session, Exception> ok -> {
                ok.value().setRewarded();
                player.sendMessage(
                        Component.text(cm.getRewardMessage()).color(NamedTextColor.GOLD));
                for (String rewardString : cm.getRewards()) {
                    String parsedCommand = Util.parseCommand(player, rewardString);

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                    log.info("Sent reward of: " + parsedCommand + " to " + player.getName());
                }
                Bukkit.getPluginManager().callEvent(new IncrementRewardCountEvent());
            }
            case Err<Session, Exception> err -> {
                log.err(
                        String.format(
                                "Failed to reward player %s due to session retrieval"
                                        + " error: %s",
                                player.getName(), err.error().getMessage()));
            }
        }
    }
}
