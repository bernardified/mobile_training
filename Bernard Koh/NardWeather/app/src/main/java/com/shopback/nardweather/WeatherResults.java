package com.shopback.nardweather;

class WeatherResults {

    private final String city, lastUpdated, details, temperature, weatherIcon;
    private final int order;

    WeatherResults(String city, String lastUpdated, String details, String temperature, String weatherIcon, int order) {
        this.city = city;
        this.lastUpdated = lastUpdated;
        this.details = details;
        this.temperature = temperature;
        this.weatherIcon = weatherIcon;
        this.order = order%3;
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

    String getWeatherIcon() {
        return weatherIcon;
    }

    int getOrder() {
        return order;
    }
}
