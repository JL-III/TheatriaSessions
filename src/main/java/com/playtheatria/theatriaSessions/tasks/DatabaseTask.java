package com.playtheatria.theatriaSessions.tasks;

import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.ResetTimeRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class DatabaseTask extends BukkitRunnable {
    private final SessionRepository sessionRepository;
    private final ResetTimeRepository resetTimeRepository;
    private final SessionManager sessionManager;

    public DatabaseTask(SessionRepository sessionRepository, ResetTimeRepository resetTimeRepository, SessionManager sessionManager) {
        this.sessionRepository = sessionRepository;
        this.resetTimeRepository = resetTimeRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        Util.sendFormattedLog("Persisting current sessions now. " + sessionManager.getSessions().size() + " sessions found." );
        for (Session session : sessionManager.getSessions()) {
            if (!sessionRepository.createOrUpdate(session)) {
                Util.sendFormattedLog("Error persisting session to database " + session.getPlayerName() + session.getSessionTime());
            }
        }
        resetTimeRepository.saveResetTime(LocalDateTime.now());
    }
}
