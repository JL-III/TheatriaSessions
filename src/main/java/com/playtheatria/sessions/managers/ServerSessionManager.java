package com.playtheatria.sessions.managers;

import com.playtheatria.sessions.database.data.ServerSession;
import org.jetbrains.annotations.NotNull;

public class ServerSessionManager {
    private ServerSession serverSession;

    public ServerSessionManager(@NotNull ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    public void setServerSession(@NotNull ServerSession serverSession) {
        this.serverSession = serverSession;
    }

    public @NotNull ServerSession getServerSession() {
        return serverSession;
    }
}
