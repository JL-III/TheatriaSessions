package com.playtheatria.theatriaSessions.database.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.playtheatria.jliii.generalutils.utils.TimeUtils;
import com.playtheatria.theatriaSessions.enums.HistoryType;
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
        this.timestamp = LocalDateTime.now(TimeUtils.timeZone).toString();
    }

    public Price(HistoryType historyType, LocalDateTime timestamp, Material material, double price) {
        this.historyType = historyType.name();
        this.material = material.name();
        this.price = price;
        this.timestamp = timestamp.toString();
    }

    public HistoryType getHistoryType() {
        return HistoryType.valueOf(historyType);
    }

    public Material getMaterial() {
        return  Material.valueOf(material);
    }

    public double getPrice() {
        return price;
    }

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp);
    }
}
