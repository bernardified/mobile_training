package com.shopback.nardweather;


class WeatherResults {
    public enum ResultType {
        NORMAL, OFFLINE;
    }

    private final String city, lastUpdated, details, temperature, weatherIcon;
    private ResultType type;


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

    void setResulType(ResultType type) {
        this.type = type;
    }

    ResultType getResultType() {
        return type;
    }
}
