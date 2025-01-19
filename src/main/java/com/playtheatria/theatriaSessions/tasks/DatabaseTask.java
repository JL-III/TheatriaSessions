package com.playtheatria.theatriaSessions.tasks;

import com.playtheatria.theatriaSessions.database.data.Session;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

public class DatabaseTask extends BukkitRunnable {
    private final SessionRepository sessionRepository;
    private final SessionManager sessionManager;

    public DatabaseTask(
            SessionRepository sessionRepository,
            SessionManager sessionManager
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        Util.sendFormattedLog("Persisting data now. " + sessionManager.getSessions().size() + " sessions found." );
        for (Session session : sessionManager.getSessions().values()) {
            if (!sessionRepository.createOrUpdate(session)) {
                Util.sendFormattedLog("Error persisting session to database " + session.getPlayerName() + session.getSessionTime());
            }
        }
    }
}
