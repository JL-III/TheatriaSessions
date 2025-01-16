package com.playtheatria.theatriaSessions.listeners;

import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.database.repositories.PriceRepository;
import com.playtheatria.theatriaSessions.database.repositories.SessionRepository;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import com.playtheatria.theatriaSessions.events.HourChangeEvent;
import com.playtheatria.theatriaSessions.managers.PriceManager;
import com.playtheatria.theatriaSessions.managers.SessionManager;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.utils.CustomLogger;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.LocalDateTime;
import java.util.List;

public class HourChangeListener implements Listener {
    private final PriceManager priceManager;
    private final PriceRepository priceRepository;
    private final SessionManager sessionManager;
    private final SessionRepository sessionRepository;
    private final CustomLogger customLogger;

    public HourChangeListener(
            PriceManager priceManager,
            PriceRepository priceRepository,
            SessionManager sessionManager,
            SessionRepository sessionRepository,
            CustomLogger customLogger
    ) {
        this.priceManager = priceManager;
        this.priceRepository = priceRepository;
        this.sessionManager = sessionManager;
        this.sessionRepository = sessionRepository;
        this.customLogger = customLogger;
    }

    @EventHandler
    public void onHourChange(HourChangeEvent event) {
        if (Util.isNewDay(event.getLastResetHour(), event.getNow())) {
            Util.sendFormattedLog("Day change detected. Clearing sessions.");
            sessionManager.resetSessions();
            sessionRepository.purgeAll();
        }

        handleGraduationLogic(event.getLastResetHour(), event.getNow());

        // Save PricesToBeSaved to database
        customLogger.sendDebug("HourChangeListener saving prices from pricesToBeSaved to database.");
        for (Price price : priceManager.getPrices()) {
            priceRepository.createOrUpdate(price);
        }

        // Cleanup price manager and price repository
        customLogger.sendDebug("Clearing prices to be saved.");
        priceRepository.cleanupOldDatabaseEntries();

        // Load historical prices from database
        switch (priceRepository.loadPrices()) {
            case Ok<List<Price>, Exception> ok -> {
                priceManager.setPrices(ok.value());
            }
            case Err<List<Price>, Exception> err -> {
                Util.sendFormattedLog("Error getting historical prices from repository!");
                Util.sendFormattedLog(err.error().getMessage());
            }
        }

        // Trigger a price calculation if the hour is divisible by 6.
        if (event.getNow().getHour() % 6 == 0) {
            customLogger.sendDebug(String.format("Calculating prices at %s!", event.getNow().atZone(Util.timeZone)));
            priceManager.setPrices(priceManager.calculatePrices());
        }
    }

    /**
     * Compares the lastResetHour and the current hour and determines if a price needs to graduate to it's respective longer period.
     * @param lastResetHour The last hour that was stored in the database
     * @param now The current hour
     */
    private void handleGraduationLogic(LocalDateTime lastResetHour, LocalDateTime now) {
        // Persist the last hourly price as a daily price if a new day has started
        if (Util.isNewDay(lastResetHour, now)) {
            graduate(HistoryType.HOURLY, HistoryType.DAILY);
        }

        // Persist daily prices as weekly prices if a new week has started
        if (Util.isNewWeek(lastResetHour, now)) {
            graduate(HistoryType.DAILY, HistoryType.WEEKLY);
        }

        // Persist weekly prices as monthly prices if a new month has started
        if (Util.isNewMonth(lastResetHour, now)) {
            graduate(HistoryType.WEEKLY, HistoryType.MONTHLY);
        }

        // Persist monthly prices as yearly prices if a new year has started
        if (Util.isNewYear(lastResetHour, now)) {
            graduate(HistoryType.MONTHLY, HistoryType.YEARLY);
        }
    }

    private void graduate(HistoryType original, HistoryType target) {
        switch (priceManager.getLastPrice(original)) {
            case Ok<Price, Exception> ok -> {
                customLogger.sendDebug("Found " + original + " price in cache.");
                customLogger.sendDebug(Util.formatPrice(ok.value()));

                // Side effect application
                handleGraduatedPrice(calculateGraduatedPrice(target, ok.value()));
            }
            case Err<Price, Exception> err -> customLogger.sendDebug(err.error().getMessage());
        }
    }

    private Price calculateGraduatedPrice(HistoryType target, Price lastPrice) {
        return new Price(target, lastPrice.getMaterial(), lastPrice.getPrice());
    }

    private void handleGraduatedPrice(Price price) {
        priceRepository.createOrUpdate(price);
        priceManager.addPrice(price);
        customLogger.sendDebug("Price updated: " + Util.formatPrice(price));
    }
}
