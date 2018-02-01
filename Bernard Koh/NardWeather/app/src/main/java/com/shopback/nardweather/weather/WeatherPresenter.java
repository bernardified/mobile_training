package com.shopback.nardweather.weather;

import android.content.Context;
import android.util.Log;

import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.data.WeatherDataSource;
import com.shopback.nardweather.data.WeatherStorage;
import com.shopback.nardweather.util.ThreadManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherPresenter implements WeatherContract.Presenter {

    private final WeatherStorage weatherStorage;

    private final WeatherContract.View weatherView;

    private final ThreadManager threadManager;

    private boolean firstLoad = true;

    WeatherPresenter(WeatherStorage weatherStorage, WeatherContract.View weatherView,
                            ThreadManager threadManager) {
        this.weatherStorage = weatherStorage;
        this.weatherView = weatherView;
        this.weatherView.setPresenter(this);
        this.threadManager = threadManager;
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

        weatherStorage.getWeatherList(new WeatherDataSource.LoadWeatherCallback() {
            @Override
            public void onWeatherLoaded(List<Weather> weatherList) {
                List<Weather> outdatedList;
                //refreshes all weather info stored in database
                if (forceUpdate) {
                    outdatedList = weatherList;
                } else {
                    outdatedList = checkOutdatedInfo(weatherList);
                }

                if (outdatedList.size() == 0) {
                    if (showLoadingUI) {
                        weatherView.setLoadingIndicator(false);
                    }
                    weatherView.showWeather(weatherList);
                } else {
                    processWeatherList(outdatedList);
                }
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
            threadManager.getNetworkThreadPool().execute(getFetchWeatherRunnable(city));
        }
    }

    @Override
    public void deleteCity(String weatherId) {
        weatherStorage.deleteWeather(weatherId);
    }

    @Override
    public void stop() {

    }

    /**
     * Creates a Runnable object to fetch weather information
     *
     * @param city: String
     * @return Runnable
     */
    private Runnable getFetchWeatherRunnable(final String city) {
        return new Runnable() {
            @Override
            public void run() {
                final Weather data = FetchWeather.getWeather(city);
                if (data != null) {
                    weatherStorage.saveWeather(data);
                    threadManager.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            loadWeather(false, false);
                        }
                    });
                }
            }
        };
    }

    private void processWeatherList(List<Weather> outdatedList) {
        List<String> refreshCities = new ArrayList<>();
        for (Weather outdated: outdatedList) {
            deleteCity(outdated.getId());
            refreshCities.add(outdated.getCity());
        }
        addNewCities(refreshCities);
    }

    private List<Weather> checkOutdatedInfo(List<Weather> originalList) {
        List<Weather> outdatedList = new ArrayList<>();

        for (Weather weather : originalList) {
            if (weather.getResultType() == Weather.ResultType.OFFLINE.ordinal()) {
                outdatedList.add(weather);
                Log.d("Refreshing", weather.getCity());
            } else {
                long currTime = new Date().getTime();
                long prevTime, diff, minutes;
                DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss aaa");
                try {
                    prevTime = formatter.parse(weather.getLastUpdated()).getTime();
                    diff = currTime - prevTime;
                    minutes = diff / (60 * 1000);
                    if (minutes >= 60) {
                        outdatedList.add(weather);
                        Log.d("Refreshing", weather.getCity());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return outdatedList;
    }
}
