package com.shopback.nardweather;

class WeatherResults {

    private final String city, lastUpdated, details, temperature, weatherIcon;

    WeatherResults(String city, String lastUpdated, String details, String temperature, String weatherIcon) {
        this.city = city;
        this.lastUpdated = lastUpdated;
        this.details = details;
        this.temperature = temperature;
        this.weatherIcon = weatherIcon;
    }

    String getCity() {
        return city;
    }

    String getLastUpdated() {
        return lastUpdated;
    }

   // String getDetails() { return details; }

    String getTemperature() {
        return temperature;
    }

    String getWeatherIcon() {
        return weatherIcon;
    }
}
