package com.playtheatria.theatriaSessions.tasks;

import com.playtheatria.theatriaSessions.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.ResetTimeRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.managers.ResetTimeManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class DatabaseTask extends BukkitRunnable {
    private final ResetTimeRepository resetTimeRepository;
    private final ResetTimeManager resetTimeManager;
    private final SessionRepository sessionRepository;
    private final SessionManager sessionManager;


    public DatabaseTask(ResetTimeRepository resetTimeRepository, ResetTimeManager resetTimeManager, SessionRepository sessionRepository, SessionManager sessionManager) {
        this.resetTimeRepository = resetTimeRepository;
        this.resetTimeManager = resetTimeManager;
        this.sessionRepository = sessionRepository;
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
        resetTimeRepository.saveResetTime(resetTimeManager.getResetTime());
    }
}
