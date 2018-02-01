package com.shopback.nardweather.data.local;

import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.data.WeatherDataSource;
import com.shopback.nardweather.util.ThreadManager;

import java.util.List;


public class LocalWeatherDataSource implements WeatherDataSource {

    private static LocalWeatherDataSource localInstance;

    private WeatherDao weatherDao;

    private ThreadManager threadManager;

    private LocalWeatherDataSource(ThreadManager threadManager, WeatherDao weatherDao) {
        this.weatherDao = weatherDao;
        this.threadManager = threadManager;
    }

    public static LocalWeatherDataSource getInstance(ThreadManager threadManager,
                                                     WeatherDao weatherDao) {
        if (localInstance == null) {
            synchronized (LocalWeatherDataSource.class) {
                if (localInstance == null) {
                    localInstance = new LocalWeatherDataSource(threadManager, weatherDao);
                }
            }
        }
        return localInstance;
    }

    @Override
    public void getWeatherList(final LoadWeatherCallback callback) {
        Runnable loadRunnable = new Runnable() {
            @Override
            public void run() {
                final List<Weather> weatherList = weatherDao.getAllWeather();
                threadManager.mainThread().execute(new Runnable(){

                    @Override
                    public void run() {
                        if (weatherList.isEmpty()) {
                            callback.onDataNotAvailable();
                        } else {
                            callback.onWeatherLoaded(weatherList);
                        }
                    }
                });
            }
        };

        threadManager.getDiskThreadPool().execute(loadRunnable);
    }

    @Override
    public void saveWeather(final Weather weather) {
        Runnable saveRunnable = new Runnable() {

            @Override
            public void run() {
                weatherDao.insertWeather(weather);
            }
        };
        threadManager.getDiskThreadPool().execute(saveRunnable);
    }

    @Override
    public void deleteWeather(final String weatherId) {
        Runnable deleteRunnable = new Runnable() {

            @Override
            public void run() {
                weatherDao.deleteWeatherById(weatherId);
            }
        };
        threadManager.getDiskThreadPool().execute(deleteRunnable);
    }

    @Override
    public void updateWeather(final Weather weather) {
        Runnable updateRunnable = new Runnable() {

            @Override
            public void run() {
                weatherDao.updateWeather(weather);
            }
        };
        threadManager.getDiskThreadPool().execute(updateRunnable);
    }
}
