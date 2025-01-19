package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.events.time.DayChangeEvent;
import com.playtheatria.jliii.generalutils.events.time.HourChangeEvent;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.events.PricesGraduationEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listen for {@link HourChangeEvent}, if this is a new day, fire a {@link DayChangeEvent}.
 * No matter what we fire a {@link PricesGraduationEvent}.
 */
public class HourChangeListener implements Listener {
    private final PriceManager priceManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public HourChangeListener(PriceManager priceManager, CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.priceManager = priceManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onHourChange(HourChangeEvent event) {
        customLogger.sendDebug("[HourChangeEvent] fired, checking if new day has started since last reset hour.");
        if (TimeUtils.isNewDay(event.getLastHour(), event.getNow())) {
            Bukkit.getPluginManager().callEvent(new DayChangeEvent());
        }
        customLogger.sendDebug("[HourChangeEvent] Calling PricesGraduationEvent.");
        Bukkit.getPluginManager().callEvent(new PricesGraduationEvent(priceManager.getPriceListCache(), event.getLastHour(), event.getNow()));
    }
}
