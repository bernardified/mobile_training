package com.shopback.nardweather.data;


import android.test.LoaderTestCase;

import java.util.List;

public interface WeatherDataSource {


    interface LoadWeatherCallback {

        void onWeatherLoaded(List<Weather> weatherList);

        void onDataNotAvailable();
    }

    interface FetchWeatherCallback {

        void onWeatherFetched(Weather weather);

        void onDataNotFetched();
    }

    void getWeatherList(LoadWeatherCallback callback);

    void getWeather(String city, FetchWeatherCallback callback);

    void saveWeather(Weather weather);

    void deleteWeather(String weatherId);

    void deleteAllWeather();

    void updateWeather(Weather weather);

    void refreshAll();

}
