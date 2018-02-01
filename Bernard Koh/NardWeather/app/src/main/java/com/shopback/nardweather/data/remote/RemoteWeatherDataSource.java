package com.shopback.nardweather.data.remote;

import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.data.WeatherDataSource;
import com.shopback.nardweather.data.local.LocalWeatherDataSource;
import com.shopback.nardweather.util.ThreadManager;


public class RemoteWeatherDataSource implements WeatherDataSource {

    private static RemoteWeatherDataSource remoteInstance;

    private ThreadManager threadManager;

    private RemoteWeatherDataSource(ThreadManager threadManager) {
        this.threadManager = threadManager;
    }

    public static RemoteWeatherDataSource getInstance(ThreadManager threadManager) {
        if (remoteInstance == null) {
            synchronized (LocalWeatherDataSource.class) {
                if (remoteInstance == null) {
                    remoteInstance = new RemoteWeatherDataSource(threadManager);
                }
            }
        }
        return remoteInstance;
    }


    @Override
    public void getWeatherList(LoadWeatherCallback callback) {
    //not applicable to the remote server
    }

    @Override
    public void getWeather(final String city, final FetchWeatherCallback callback) {
        Runnable getFetchWeatherRunnable = new Runnable() {

            @Override
            public void run() {
                final Weather weather = FetchWeather.getWeather(city);

                threadManager.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null) {
                            callback.onWeatherFetched(weather);
                        } else {
                            callback.onDataNotFetched();
                        }
                    }
                });
            }
        };
        threadManager.getNetworkThreadPool().execute(getFetchWeatherRunnable);
    }

    @Override
    public void saveWeather(Weather weather) {
    //not applicable to remote server
    }

    @Override
    public void deleteWeather(String weatherId) {
    //not applicable to remote server
    }

    @Override
    public void deleteAllWeather() {
        //not applicable
    }

    @Override
    public void updateWeather(Weather weather) {

    }

    @Override
    public void refreshAll() {

    }
}
