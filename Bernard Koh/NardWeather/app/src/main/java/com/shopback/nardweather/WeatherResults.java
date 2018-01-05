package com.shopback.nardweather;

/**
 * Created by bernardkoh on 5/1/18.
 */

public class WeatherResults {
    private String city, lastUpdated, details, temperature;
    private long sunrise, sunset;
    private int weatherIcon;

    void setCity(String city){
        this.city = city;
    }

    void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    void setDetails(String details) {
        this.details = details;
    }

    void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    void setWeatherIcon(int weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    void setSunset(long sunset) {
        this.sunset = sunset;
    }

    String getCity() {
        return city;
    }

    String getLastUpdated() {
        return lastUpdated;
    }

    String getDetails() { return details; }

    String getTemperature() {
        return temperature;
    }

    int getWeatherIcon() {
        return weatherIcon;
    }

    long getSunrise() {
        return sunrise;
    }

    long getSunset() {
        return sunset;
    }
}
