package com.playtheatria.theatriaSessions.events;

import com.playtheatria.theatriaSessions.database.data.Price;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This event exists to handle persisting the cached price list into the database.
 */
public class PricesSaveToDatabaseEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Price> priceList;
    private final LocalDateTime now;

    public PricesSaveToDatabaseEvent(List<Price> priceList, LocalDateTime now) {
        this.priceList = priceList;
        this.now = now;
    }

    public List<Price> getPriceList() {
        return priceList;
    }

    public LocalDateTime getNow() {
        return now;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
