package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.jliii.generalutils.result.Err;
import com.playtheatria.jliii.generalutils.result.Ok;
import com.playtheatria.jliii.generalutils.utils.CustomLogger;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.TheatriaSessions;
import com.playtheatria.theatriaSessions.config.ConfigManager;
import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import com.playtheatria.theatriaSessions.events.PricesGraduationEvent;
import com.playtheatria.theatriaSessions.events.PricesSaveToDatabaseEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listens for the PricesGraduationEvent and processes the list provided in the event.
 * Checks for any eligible price graduations and appends them to the list provided.
 * Fires the PricesSaveToDatabaseEvent and passes the new list into it.
 */
public class PricesGraduationListener implements Listener {
    private final CustomLogger<TheatriaSessions, ConfigManager> customLogger;

    public PricesGraduationListener(CustomLogger<TheatriaSessions, ConfigManager> customLogger) {
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onPricesGraduation(PricesGraduationEvent event) {
        customLogger.sendDebug("[PricesGraduationEvent] handling graduation logic.");
        customLogger.sendDebug("[PricesGraduationEvent] Calling PricesSaveToDatabaseEvent.");
        Bukkit.getPluginManager().callEvent(
                new PricesSaveToDatabaseEvent(
                        handleGraduationLogic(event.getLastResetHour(), event.getNow(), event.getPriceList()),
                        event.getNow()
                )
        );
    }

    /**
     * Compares the lastResetHour and the current hour and determines if a price needs to graduate to its respective longer period.
     * @param lastResetHour The last hour that was stored in the database
     * @param now The current hour
     */
    private List<Price> handleGraduationLogic(LocalDateTime lastResetHour, LocalDateTime now, List<Price> priceList) {
        // Persist the last hourly price as a daily price if a new day has started
        List<Price> allPrices = new ArrayList<>(priceList);
        if (TimeUtils.isNewDay(lastResetHour, now)) {
            allPrices.addAll(graduate(HistoryType.HOURLY, HistoryType.DAILY, priceList));
        }

        // Persist daily prices as weekly prices if a new week has started
        if (TimeUtils.isNewWeek(lastResetHour, now)) {
            allPrices.addAll(graduate(HistoryType.DAILY, HistoryType.WEEKLY, priceList));
        }

        // Persist weekly prices as monthly prices if a new month has started
        if (TimeUtils.isNewMonth(lastResetHour, now)) {
            allPrices.addAll(graduate(HistoryType.WEEKLY, HistoryType.MONTHLY, priceList));
        }

        // Persist monthly prices as yearly prices if a new year has started
        if (TimeUtils.isNewYear(lastResetHour, now)) {
            allPrices.addAll(graduate(HistoryType.MONTHLY, HistoryType.YEARLY,  priceList));
        }
        return allPrices;
    }

    private List<Price> graduate(HistoryType original, HistoryType target, List<Price> priceList) {
        Set<Material> materials = priceList.stream().map(Price::getMaterial).collect(Collectors.toSet());
        List<Price> newPriceList = new ArrayList<>();
        for (Material material : materials) {
            switch (PriceManager.getLastPrice(original, material, priceList)) {
                case Ok<Price, Exception> ok -> {
                    customLogger.sendDebug("[PricesGraduationEvent] Found " + original + " price for " + material + " in provided Price List.");
                    customLogger.sendDebug("[PricesGraduationEvent] " + Util.formatPrice(ok.value()));

                    newPriceList.add(
                            new Price(target, getEndOfPreviousDay(LocalDateTime.now(TimeUtils.timeZone)), ok.value().getMaterial(), ok.value().getPrice())
                    );
                }
                case Err<Price, Exception> err -> customLogger.sendDebug("[PricesGraduationEvent] " + err.error().getMessage());
            }
        }
        return newPriceList;
    }

    public LocalDateTime getEndOfPreviousDay(LocalDateTime now) {
        return now.minusDays(1)
                .withHour(23)
                .withMinute(59)
                .withSecond(0)
                .withNano(0);
    }
}
