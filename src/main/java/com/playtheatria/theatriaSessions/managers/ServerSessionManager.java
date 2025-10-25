package com.playtheatria.theatriaSessions.managers;

import com.playtheatria.theatriaSessions.database.data.ServerSession;

public class ServerSessionManager {
    private ServerSession serverSession;

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
