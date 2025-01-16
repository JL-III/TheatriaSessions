package com.playtheatria.theatriaSessions.managers;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import com.playtheatria.theatriaSessions.database.data.Price;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.result.Result;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The PriceManager holds the prices containing all historical data in memory.
 * This includes, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY historical data types.
 * The idea is that this allows players to query the data quickly without performance penalties.
 * The price manager will also hold "soon to be saved" data.
 */
public class PriceManager {
    private final Essentials essentials;
    private List<Price> prices = new ArrayList<>();

    public PriceManager(Essentials essentials, List<Price> priceList) {
        this.essentials = essentials;
        setPrices(priceList);
    }

    /**
     * Storing local cache of yet to be persisted prices.
     * @param priceList The price list loaded from calculatePrices() method
     */
    public void setPrices(List<Price> priceList) {
        prices = List.copyOf(priceList);
    }

    public void addPrice(Price price) {
        List<Price> newPrices = new ArrayList<>(prices);
        newPrices.add(price);
        prices = List.copyOf(newPrices);
    }

    public List<Price> getPrices() {
        return List.copyOf(prices);
    }

    /**
     * Fetches prices for materials from Essentials, adjusts them using a pricing algorithm,
     * and prepares them to be saved to the database.
     */
    public @NotNull List<Price> calculatePrices() {
        List<Material> materials = List.of(
                Material.DIAMOND,
                Material.TROPICAL_FISH,
                Material.PUFFERFISH,
                Material.GOLD_INGOT,
                Material.NETHERITE_INGOT,
                Material.IRON_INGOT
        );

        Worth worth = essentials.getWorth();

        List<Price> newPricesToBeSaved = new ArrayList<>(prices);
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

            newPricesToBeSaved.add(new Price(
                    HistoryType.HOURLY,
                    material,
                    adjustedPrice.doubleValue()));
        }
        return newPricesToBeSaved;
    }

    public Result<Price, Exception> getLastPrice(HistoryType historyType) {
        Optional<Price> optionalPrice = prices
                .stream()
                .filter(x -> x.getHistoryType().equals(historyType))
                .max(Comparator.comparing(Price::getTimestamp));
        if (optionalPrice.isPresent()) {
            return new Ok<>(optionalPrice.get());
        } else {
            return new Err<>(new Exception("Could not find the last price in PriceManager for history type of " + historyType));
        }
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
