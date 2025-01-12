package com.playtheatria.theatriaSessions.managers;

import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Material;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The PriceManager holds the prices containing all historical data in memory.
 * This includes, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY historical data types.
 * The idea is that this allows players to query the data quickly without performance penalties.
 * The price manager will also hold "soon to be saved" data.
 */
public class PriceManager {
    private CopyOnWriteArrayList<Price> historicalPrices;
    private ConcurrentHashMap<Material, Price> pricesToBeSaved = new ConcurrentHashMap<>();

    public PriceManager(List<Price> priceList) {
        this.historicalPrices = new CopyOnWriteArrayList<>(priceList);
    }

    public void addPriceToPricesToBeSaved(Price price) {
        switch (price.getMaterial()) {
            case Ok<Material, Exception> ok -> {
                pricesToBeSaved.put(ok.value(), price);
            }
            case Err<Material, Exception> err -> {
                Util.sendFormattedLog("Error trying to add price: %s" + err.error().getMessage());
            }
        }
    }

    /**
     * Called when the Price Repository has successfully persisted this data to the database.
     * The pricesToBeSaved are calculated at HourChangeEvents, the information stays in this class until persisted.
     */
    public void resetPricesToBeSaved() {
        pricesToBeSaved = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<Material, Price> getPricesToBeSaved() {
        return pricesToBeSaved;
    }

    public CopyOnWriteArrayList<Price> getHistoricalPrices() {
        return historicalPrices;
    }

    /**
     * Use this method once pricesToBeSaved have been successfully saved.
     * @param priceList Provide the Historical Price list from the database.
     */
    public void updateHistoricalPrices(List<Price> priceList) {
        historicalPrices = new CopyOnWriteArrayList<>(priceList);
    }

}
