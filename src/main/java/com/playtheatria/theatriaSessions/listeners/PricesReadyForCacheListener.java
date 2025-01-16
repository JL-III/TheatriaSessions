package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.events.PricesReadyForCacheEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Called when the price list is ready to be saved to the cache found in the {@link PriceManager}
 */
public class PricesReadyForCacheListener implements Listener {
    private final PriceManager priceManager;
    private final CustomLogger customLogger;

    public PricesReadyForCacheListener(PriceManager priceManager, CustomLogger customLogger) {
        this.priceManager = priceManager;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onPricesReady(PricesReadyForCacheEvent event) {
        customLogger.sendDebug("[PricesReadyForCacheEvent] setting price list cache on PriceManager.");
        priceManager.setPriceListCache(event.getPriceList());
    }
}
