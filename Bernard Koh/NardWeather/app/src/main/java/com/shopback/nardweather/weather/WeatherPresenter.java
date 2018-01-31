package com.shopback.nardweather.weather;

import android.content.Context;
import android.util.Log;

import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.data.WeatherDataSource;
import com.shopback.nardweather.data.WeatherStorage;
import com.shopback.nardweather.util.ThreadManager;
import java.util.List;

public class WeatherPresenter implements WeatherContract.Presenter {

    private final WeatherStorage weatherStorage;

    private final WeatherContract.View weatherView;

    private final ThreadManager threadManager;

    private boolean firstLoad = true;

    public WeatherPresenter(WeatherStorage weatherStorage, WeatherContract.View weatherView,
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

    private void loadWeather(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            weatherView.setLoadingIndicator(true);
        }

        if (forceUpdate) {
            weatherStorage.refreshAllWeather();
        }

        weatherStorage.getWeatherList(new WeatherDataSource.LoadWeatherCallback() {
            @Override
            public void onWeatherLoaded(List<Weather> weatherList) {
                List<Weather> weatherToShow = weatherList;
                if (showLoadingUI) {
                    weatherView.setLoadingIndicator(false);
                }
                weatherView.showWeather(weatherToShow);
            }

            @Override
            public void onDataNotAvailable() {
                weatherView.showMessage("Error loading error from database");
                Log.e("Weather Presenter", "error loading from database");
            }
        });
    }

    /**
     * Fetches the weather info of the input cities
     *
     * @param newCities: List retrieved from UI input
     */
    @Override
    public void addNewCities(Context context, List<String> newCities) {
        if (newCities.isEmpty()) {
            return;
        }
        for (String city: newCities) {
            threadManager.getNetworkThreadPool().execute(getFetchWeatherRunnable(context, city));
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
    private Runnable getFetchWeatherRunnable(final Context context, final String city) {
        return new Runnable() {
            @Override
            public void run() {
                final Weather data = FetchWeather.getWeather(context, city);
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
}
