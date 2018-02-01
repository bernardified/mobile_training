package com.shopback.nardweather.data;


import android.test.LoaderTestCase;

import java.util.List;

public interface WeatherDataSource {

    interface LoadWeatherCallback {

        void onWeatherLoaded(List<Weather> weatherList);

        void onDataNotAvailable();
    }

    void getWeatherList(LoadWeatherCallback callback);

    void saveWeather(Weather weather);

    void deleteWeather(String weatherId);

    void updateWeather(Weather weather);

}
