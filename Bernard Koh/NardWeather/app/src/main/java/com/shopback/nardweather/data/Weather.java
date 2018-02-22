package com.shopback.nardweather.data;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.UUID;


@Entity(tableName = "weather")
public class Weather {

    public enum ResultType {
        NORMAL, OFFLINE
    }

    @NonNull
    @ColumnInfo(name = "id")
    private final String id;

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "city")
    private final String city;

    @ColumnInfo(name = "lastUpdated")
    private final String lastUpdated;

    @ColumnInfo(name = "details")
    private final String details;

    @ColumnInfo(name = "temperature")
    private final String temperature;

    @ColumnInfo(name = "icon")
    private final String weatherIcon;

    @ColumnInfo(name = "resultType")
    private int resultType;

    @Ignore
    public Weather(String city, String lastUpdated, String details, String temperature, String weatherIcon) {
        this(UUID.randomUUID().toString(), city, lastUpdated, details, temperature, weatherIcon);
    }

    public Weather(String id, String city, String lastUpdated, String details, String temperature, String weatherIcon) {
        this.id = id;
        this.city = city;
        this.lastUpdated = lastUpdated;
        this.details = details;
        this.temperature = temperature;
        this.weatherIcon = weatherIcon;
    }

    public String getCity() {
        return city;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getDetails() { return details; }

    public String getTemperature() {
        return temperature;
    }

    public String getWeatherIcon() {
        return weatherIcon;
    }

    public String getId() {return id;}

    public void setResultType(int type) {
        this.resultType = type;
    }

    public int getResultType() {
        return resultType;
    }
}
