package com.playtheatria.sessions.events;

import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RewardPlayerEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID playerUUID;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public RewardPlayerEvent(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }
}
