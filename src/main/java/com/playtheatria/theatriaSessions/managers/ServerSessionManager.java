package com.playtheatria.theatriaSessions.managers;

import com.playtheatria.theatriaSessions.data.ServerSession;


public class ServerSessionManager {
    private ServerSession serverSession;

    /**
     *
     * @param serverSession provide the ServerSession from the database or create a new one if one for this date doesn't exist.
     */
    public ServerSessionManager(ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    public void setServerSession(ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    public ServerSession getServerSession() {
        return serverSession;
    }

}
