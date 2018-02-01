package com.shopback.nardweather.weather;


import android.util.Log;

import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.data.WeatherDataSource;
import com.shopback.nardweather.data.WeatherStorage;

import java.util.List;

public class WeatherPresenter implements WeatherContract.Presenter {

    private final WeatherStorage weatherStorage;

    private final WeatherContract.View weatherView;

    private boolean firstLoad = true;

    WeatherPresenter(WeatherStorage weatherStorage, WeatherContract.View weatherView) {
        this.weatherStorage = weatherStorage;
        this.weatherView = weatherView;
        this.weatherView.setPresenter(this);
    }


    @Override
    public void start() {
        loadWeather(false);
    }

    @Override
    public void loadWeather(boolean forceUpdate) {
        loadWeather(forceUpdate||firstLoad, true);
        firstLoad = false;
    }

    private void loadWeather(final boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            weatherView.setLoadingIndicator(true);
        }

      /*  if (forceUpdate) {
            weatherStorage.refreshAll();
        }*/

        weatherStorage.getWeatherList(new WeatherDataSource.LoadWeatherCallback() {
            @Override
            public void onWeatherLoaded(List<Weather> weatherList) {

                if (showLoadingUI) {
                    weatherView.setLoadingIndicator(false);
                }
                    weatherView.showWeather(weatherList);
            }

            @Override
            public void onDataNotAvailable() {
                weatherView.showMessage("Error loading error from database");
                Log.e("Weather Presenter", "error loading from database");
            }
        });
    }

    @Override
    public void addNewCities(List<String> newCities) {
        if (newCities.isEmpty()) {
            return;
        }
        for (String city: newCities) {
            weatherStorage.getWeather(city, new WeatherDataSource.FetchWeatherCallback() {
                @Override
                public void onWeatherFetched(Weather weather) {
                    loadWeather(false, false);
                }

                @Override
                public void onDataNotFetched() {
                    weatherView.showMessage("Error retrieving weather info ");
                    Log.e("Weather Presenter", "error loading from database");
                }
            });
        }

    }

    @Override
    public void deleteCity(String weatherId) {
        weatherStorage.deleteWeather(weatherId);
    }

    @Override
    public void stop() {

    }

}
