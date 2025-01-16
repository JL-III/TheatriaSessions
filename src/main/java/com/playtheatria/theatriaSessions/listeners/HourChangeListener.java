package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.DayChangeEvent;
import com.playtheatria.theatriaSessions.events.HourChangeEvent;
import com.playtheatria.theatriaSessions.events.PricesGraduationEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listen for {@link HourChangeEvent}, if this is a new day, fire a {@link DayChangeEvent}.
 * No matter what we fire a {@link PricesGraduationEvent}.
 */
public class HourChangeListener implements Listener {
    private final PriceManager priceManager;
    private final CustomLogger customLogger;

    public HourChangeListener(PriceManager priceManager, CustomLogger customLogger) {
        this.priceManager = priceManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onHourChange(HourChangeEvent event) {
        customLogger.sendDebug("[HourChangeEvent] fired, checking if new day has started since last reset hour.");
        if (Util.isNewDay(event.getLastResetHour(), event.getNow())) {
            Bukkit.getPluginManager().callEvent(new DayChangeEvent());
        }
        customLogger.sendDebug("[HourChangeEvent] Calling PricesGraduationEvent.");
        Bukkit.getPluginManager().callEvent(new PricesGraduationEvent(priceManager.getPriceListCache(), event.getLastResetHour(), event.getNow()));
    }
}
