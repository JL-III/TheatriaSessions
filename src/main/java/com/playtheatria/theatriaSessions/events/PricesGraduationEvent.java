package com.playtheatria.theatriaSessions.events;

import com.playtheatria.theatriaSessions.database.data.Price;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PricesGraduationEvent contains a price list, the last reset hour and the current time.
 */
public class PricesGraduationEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final List<Price> priceList;
    private final LocalDateTime lastResetHour;
    private final LocalDateTime now;

    public PricesGraduationEvent(List<Price> priceList, LocalDateTime lastResetHour, LocalDateTime now) {
        this.priceList = priceList;
        this.lastResetHour = lastResetHour;
        this.now = now;
    }

    public List<Price> getPriceList() {
        return priceList;
    }

    public LocalDateTime getLastResetHour() {
        return lastResetHour;
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
