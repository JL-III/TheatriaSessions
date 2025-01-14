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
import java.util.Optional;

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
            handleDayChange();
        }

        handleGraduationLogic(event.getLastResetHour(), event.getNow());

        customLogger.sendDebug("HourChangeListener saving prices from pricesToBeSaved to database.");
        for (Price price : priceManager.getPricesToBeSaved().values()) {
            priceRepository.createOrUpdate(price);

        }
        customLogger.sendDebug("Clearing prices to be saved.");
        priceManager.resetPricesToBeSaved();
        priceRepository.cleanupTask();
        switch (priceRepository.loadPrices()) {
            case Ok<List<Price>, Exception> ok -> {
                priceManager.updateHistoricalPrices(ok.value());
                priceManager.calculatePrices();
            }
            case Err<List<Price>, Exception> err -> {
                Util.sendFormattedLog("Error getting historical prices from repository!");
                Util.sendFormattedLog("New prices will not be calculated, this must be addressed!");
                Util.sendFormattedLog(err.error().getMessage());
            }
        }

    }

    public void handleDayChange() {
        Util.sendFormattedLog("Day change detected. Clearing sessions.");
        sessionManager.resetSessions();
        sessionRepository.purgeAll();
    }

    private void handleGraduationLogic(LocalDateTime lastResetHour, LocalDateTime now) {
        // Persist the last hourly price as a daily price if a new day has started
        if (Util.isNewDay(lastResetHour, now)) {
            Optional<Price> optionalPrice = priceManager.getLastPrice(HistoryType.HOURlY);
            if (optionalPrice.isPresent()) {
                Price dailyPrice = new Price(HistoryType.DAILY, optionalPrice.get().getMaterial(), optionalPrice.get().getPrice());
                priceRepository.createOrUpdate(dailyPrice);
            }
        }

        // Persist daily prices as weekly prices if a new week has started
        if (Util.isNewWeek(lastResetHour, now)) {
            Optional<Price> optionalPrice = priceManager.getLastPrice(HistoryType.DAILY);
            if (optionalPrice.isPresent()) {
                Price weeklyPrice = new Price(HistoryType.WEEKLY, optionalPrice.get().getMaterial(), optionalPrice.get().getPrice());
                priceRepository.createOrUpdate(weeklyPrice);
            }
        }

        // Persist weekly prices as monthly prices if a new month has started
        if (Util.isNewMonth(lastResetHour, now)) {
            Optional<Price> optionalPrice = priceManager.getLastPrice(HistoryType.WEEKLY);
            if (optionalPrice.isPresent()) {
                Price monthlyPrice = new Price(HistoryType.MONTHLY, optionalPrice.get().getMaterial(), optionalPrice.get().getPrice());
                priceRepository.createOrUpdate(monthlyPrice);
            }
        }

        // Persist monthly prices as yearly prices if a new year has started
        if (Util.isNewYear(lastResetHour, now)) {
            Optional<Price> optionalPrice = priceManager.getLastPrice(HistoryType.MONTHLY);
            if (optionalPrice.isPresent()) {
                Price yearlyPrice = new Price(HistoryType.YEARLY, optionalPrice.get().getMaterial(), optionalPrice.get().getPrice());
                priceRepository.createOrUpdate(yearlyPrice);
            }
        }
    }
}
