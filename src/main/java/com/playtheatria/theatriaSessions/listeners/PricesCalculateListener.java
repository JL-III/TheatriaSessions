package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.events.PricesCalculateEvent;
import com.playtheatria.theatriaSessions.events.PricesReadyForCacheEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * The {@link PricesCalculateListener} listens for the {@link PricesCalculateEvent} and passes
 * the list provided into the {@link PriceManager} for to handle the price changes.
 * After the {@link PriceManager} makes it's changes to essentials, it returns the new price list.
 * This list is then passed into the {@link PricesReadyForCacheEvent} to be saved into the {@link PriceManager}'s cache.
 */
public class PricesCalculateListener implements Listener {
    private final PriceManager priceManager;
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public PricesCalculateListener(PriceManager priceManager, CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.priceManager = priceManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onPricesCalculate(PricesCalculateEvent event) {
        customLogger.sendDebug("[PricesCalculateEvent] calling PricesReadyForCacheEvent with calculated prices from PriceManager.");
        Bukkit.getPluginManager().callEvent(new PricesReadyForCacheEvent(priceManager.calculatePrices(event.getPriceList())));
    }
}
