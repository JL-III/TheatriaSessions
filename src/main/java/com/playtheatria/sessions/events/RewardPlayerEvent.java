package com.playtheatria.sessions.events;

import com.playtheatria.sessions.database.data.Session;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RewardPlayerEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Session session;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public RewardPlayerEvent(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return this.session;
    }
}
