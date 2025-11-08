package com.playtheatria.sessions.events;

import com.playtheatria.sessions.enums.RewardTier;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RewardCommunityEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final RewardTier rewardTier;

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public RewardCommunityEvent(RewardTier rewardTier) {
        this.rewardTier = rewardTier;
    }

    public RewardTier getRewardTier() {
        return this.rewardTier;
    }
}
