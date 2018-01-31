package com.shopback.nardweather.data;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeatherStorage implements WeatherDataSource {
    private static WeatherStorage storageInstance = null;
    private final WeatherDataSource localDataSource;

    HashMap<String, Weather> cache;
    boolean isCacheDirty = false;

    public static WeatherStorage getInstance(WeatherDataSource localDatabase) {
        if (storageInstance == null) {
            storageInstance = new WeatherStorage(localDatabase);
        }
        return storageInstance;
    }

    private WeatherStorage(WeatherDataSource localDataSource) {
        this.localDataSource = localDataSource;
        cache = new HashMap<>();
    }

    public static void destroyInstance() {
        storageInstance = null;
    }

    @Override
    public void getWeatherList(final LoadWeatherCallback callback) {
        if (cache != null && !isCacheDirty) {
            callback.onWeatherLoaded(new ArrayList<>(cache.values()));
            Log.d("Weather Storage", "show weather with cache size" + cache.size());
            return;
        }
        else {
            localDataSource.getWeatherList(new LoadWeatherCallback() {
                @Override
                public void onWeatherLoaded(List<Weather> weatherList) {
                    refreshCache(weatherList);
                    callback.onWeatherLoaded(new ArrayList<>(cache.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    callback.onDataNotAvailable();
                }
            });
        }
    }

    @Override
    public void saveWeather(Weather weather) {
        cache.put(weather.getId(), weather);
        localDataSource.saveWeather(weather);
    }

    @Override
    public void deleteWeather(String weatherId) {
        cache.remove(weatherId);
        localDataSource.deleteWeather(weatherId);
    }

    @Override
    public void updateWeather(Weather weather) {
        localDataSource.updateWeather(weather);
    }

    @Override
    public void refreshAllWeather() {
        isCacheDirty = true;
    }


    private void refreshCache(List<Weather> weatherList) {
        if (cache == null) {
            cache = new HashMap<>();
        }
        cache.clear();
        for (Weather weather : weatherList) {
            cache.put(weather.getId(), weather);
        }
        isCacheDirty = false;
    }

}
