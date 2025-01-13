package com.playtheatria.theatriaSessions.managers;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * The PriceManager holds the prices containing all historical data in memory.
 * This includes, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY historical data types.
 * The idea is that this allows players to query the data quickly without performance penalties.
 * The price manager will also hold "soon to be saved" data.
 */
public class PriceManager {
    private final Essentials essentials;
    private CopyOnWriteArrayList<Price> historicalPrices;
    private ConcurrentHashMap<Material, Price> pricesToBeSaved = new ConcurrentHashMap<>();

    public PriceManager(Essentials essentials, List<Price> priceList) {
        this.essentials = essentials;
        this.historicalPrices = new CopyOnWriteArrayList<>(priceList);
    }

    public void addPriceToPricesToBeSaved(Price price) {
        pricesToBeSaved.put(price.getMaterial(), price);
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

    /**
     * Fetches prices for materials from Essentials, adjusts them using a pricing algorithm,
     * and prepares them to be saved to the database.
     */
    public void calculatePrices() {
        List<Material> materials = List.of(
                Material.DIAMOND,
                Material.TROPICAL_FISH,
                Material.PUFFERFISH,
                Material.GOLD_INGOT,
                Material.NETHERITE_INGOT,
                Material.IRON_INGOT
        );

        Worth worth = essentials.getWorth();

        // Process materials in a single loop
        for (Material material : materials) {
            BigDecimal originalPrice = worth.getPrice(essentials, new ItemStack(material));

            // Skip null prices to avoid NullPointerException
            if (originalPrice == null) {
                Util.sendFormattedLog("Price for material " + material + " is not available.");
                continue;
            }

            // Calculate adjusted price
            BigDecimal adjustedPrice = calculatePrice(originalPrice);

            // Save adjusted price in Essentials
            setPricesInEssentials(material, adjustedPrice);

            // Save the adjusted price in memory for later database persistence
            addPriceToPricesToBeSaved(new Price(
                    HistoryType.HOURlY,
                    material,
                    adjustedPrice.doubleValue()
            ));
        }
    }

    public List<Price> getPrices(HistoryType historyType) {
        return pricesToBeSaved.values()
                .stream().filter(
                        x -> x.getHistoryType().equals(historyType))
                .collect(Collectors.toList());
    }

    /**
     * Adjusts the price using a pricing algorithm. Modify this to implement custom logic.
     *
     * @param originalPrice The original price from Essentials.
     * @return The adjusted price.
     */
    public BigDecimal calculatePrice(BigDecimal originalPrice) {
        // Example: Add a 10% markup for demonstration purposes
        return originalPrice.multiply(BigDecimal.valueOf(1.10));
    }

    /**
     * Saves the price in the worth object of essentials
     * @param material The material to be saved in essentials
     * @param price The price to be set for essentials Worth
     */
    public void setPricesInEssentials(Material material, BigDecimal price) {
        essentials.getWorth().setPrice(essentials, new ItemStack(material), price.doubleValue());
    }
}
