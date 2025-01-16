package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.database.repositories.PriceRepository;
import com.playtheatria.theatriaSessions.events.PricesCalculateEvent;
import com.playtheatria.theatriaSessions.events.PricesReadyForCacheEvent;
import com.playtheatria.theatriaSessions.events.PricesSaveToDatabaseEvent;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * The {@link PricesSaveToDatabaseListener} class listens for the {@link PricesSaveToDatabaseEvent} and saves each price into the database.
 * After saving the new prices, it then cleans up the old prices in the database, based on the retention defined in the {@link PriceRepository} class.
 * Lastly, This class loads the saved prices from the {@link PriceRepository} and handles the list based on specific criteria.
 * If a price list is not successfully retrieved from the database the plugin logs the error and stops the logic here.
 * If the price list does get pulled from the database, it checks if the current hour is a divisible by 6, if so then we fire a {@link PricesCalculateEvent},
 * otherwise we take our current list and pass it into the {@link PricesReadyForCacheEvent}.
 */
public class PricesSaveToDatabaseListener implements Listener {
    private final PriceRepository priceRepository;
    private final CustomLogger customLogger;

    public PricesSaveToDatabaseListener(
            PriceRepository priceRepository,
            CustomLogger customLogger
    ) {
        this.priceRepository = priceRepository;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onPricesSaveToDatabase(PricesSaveToDatabaseEvent event) {
        // Save PricesToBeSaved to database
        customLogger.sendDebug("[PricesSaveToDatabaseEvent] saving prices to database.");
        for (Price price : event.getPriceList()) {
            priceRepository.createOrUpdate(price);
        }

        // Cleanup price manager and price repository
        customLogger.sendDebug("[PricesSaveToDatabaseEvent] Clearing prices to be saved.");
        priceRepository.cleanupOldDatabaseEntries();

        // Load historical prices from database
        switch (priceRepository.loadPrices()) {
            case Ok<List<Price>, Exception> ok -> {
                // Trigger a price calculation if the hour is divisible by 6.
                if (event.getNow().getHour() % 6 == 0) {
                    customLogger.sendDebug(String.format("[PricesSaveToDatabaseEvent] Calculating prices at %s!", event.getNow().atZone(Util.timeZone)));
                    customLogger.sendDebug("[PricesSaveToDatabaseEvent] calling PricesCalculateEvent.");
                    Bukkit.getPluginManager().callEvent(new PricesCalculateEvent(ok.value()));
                } else {
                    customLogger.sendDebug("[PricesSaveToDatabaseEvent] calling PricesReadyForCacheEvent.");
                    Bukkit.getPluginManager().callEvent(new PricesReadyForCacheEvent(ok.value()));
                }
            }
            case Err<List<Price>, Exception> err -> {
                Util.sendFormattedLog("[PricesSaveToDatabaseEvent] Error getting historical prices from repository!");
                Util.sendFormattedLog(err.error().getMessage());
            }
        }
    }
}
