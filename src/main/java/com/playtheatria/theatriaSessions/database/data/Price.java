package com.playtheatria.theatriaSessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.playtheatria.theatriaSessions.enums.HistoryType;
import com.playtheatria.theatriaSessions.result.Err;
import com.playtheatria.theatriaSessions.result.Ok;
import com.playtheatria.theatriaSessions.result.Result;
import com.playtheatria.theatriaSessions.utils.Util;
import org.bukkit.Material;

import java.time.LocalDateTime;

@DatabaseTable(tableName = "prices")
public class Price {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, index = true)
    private String historyType;

    @DatabaseField(canBeNull = false)
    private String material;

    @DatabaseField(canBeNull = false, index = true)
    private String timestamp;

    @DatabaseField(canBeNull = false)
    private double price;

    public Price() {}

    public Price(HistoryType historyType, Material material, double price) {
        this.historyType = historyType.name();
        this.material = material.name();
        this.price = price;
        this.timestamp = LocalDateTime.now(Util.timeZone).toString();
    }

    public Result<HistoryType, Exception> getHistoryType() {
        try {
            HistoryType h = HistoryType.valueOf(historyType);
            return new Ok<>(h);
        } catch (IllegalArgumentException exception) {
            return new Err<>(exception);
        }
    }

    public Result<Material, Exception> getMaterial() {
        try {
            Material m = Material.valueOf(material);
            return new Ok<>(m);
        } catch (IllegalArgumentException exception) {
            return new Err<>(exception);
        }
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp);
    }
}
