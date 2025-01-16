package com.playtheatria.theatriaSessions.events;

import com.playtheatria.theatriaSessions.database.data.Price;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PricesCalculateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Price> priceList;

    public PricesCalculateEvent(List<Price> priceList) {
        this.priceList = priceList;
    }

    public List<Price> getPriceList() {
        return priceList;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
