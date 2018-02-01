package com.shopback.nardweather.data;


import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WeatherStorage implements WeatherDataSource {
    private static WeatherStorage storageInstance = null;
    private final WeatherDataSource localDataSource;
    private final WeatherDataSource remoteDataSource;

    boolean allInfoOutdated = false;

    public static WeatherStorage getInstance(WeatherDataSource localDatabase,
                                             WeatherDataSource remoteDatabase) {
        if (storageInstance == null) {
            storageInstance = new WeatherStorage(localDatabase, remoteDatabase);
        }
        return storageInstance;
    }

    private WeatherStorage(WeatherDataSource localDataSource, WeatherDataSource remoteDataSource) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void getWeatherList(final LoadWeatherCallback callback) {
        localDataSource.getWeatherList(new LoadWeatherCallback() {
            @Override
            public void onWeatherLoaded(List<Weather> weatherList) {
                List<Weather> outdatedList;

                if (allInfoOutdated) {
                    outdatedList = weatherList;
                    allInfoOutdated = false;
                } else {
                    outdatedList = checkOutdatedInfo(weatherList);
                }

                if (outdatedList.size() != 0) {
                    refreshWeather(callback, weatherList, outdatedList);
                } else {
                    callback.onWeatherLoaded(weatherList);
                    return;
                }
            }

            @Override
            public void onDataNotAvailable() {
                remoteDataSource.getWeatherList(new LoadWeatherCallback() {
                    @Override
                    public void onWeatherLoaded(List<Weather> weatherList) {
                        refreshLocalDataSource(weatherList);
                        callback.onWeatherLoaded(weatherList);
                        return;
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
                callback.onDataNotAvailable();
            }
        });
    }

    @Override
    public void getWeather(final String city, final FetchWeatherCallback callback) {
        //checks the local database if weather is present
        localDataSource.getWeather(city, new FetchWeatherCallback() {
            @Override
            public void onWeatherFetched(Weather weather) {
                callback.onWeatherFetched(weather);
            }

            @Override
            public void onDataNotFetched() {
                remoteDataSource.getWeather(city, new FetchWeatherCallback() {
                    @Override
                    public void onWeatherFetched(Weather weather) {
                        saveWeather(weather);
                        callback.onWeatherFetched(weather);
                    }

                    @Override
                    public void onDataNotFetched() {
                        callback.onDataNotFetched();
                    }
                });
            }
        });
    }

    @Override
    public void saveWeather(Weather weather) {
        localDataSource.saveWeather(weather);
    }

    @Override
    public void deleteWeather(String weatherId) {
        localDataSource.deleteWeather(weatherId);
    }

    @Override
    public void deleteAllWeather() {
        //not applicable
    }

    @Override
    public void updateWeather(Weather weather) {
        localDataSource.updateWeather(weather);
    }

    @Override
    public void refreshAll() {
        allInfoOutdated = true;
    }


    private void refreshLocalDataSource(List<Weather> newList) {
        localDataSource.deleteAllWeather();
        for (Weather weather : newList) {
            localDataSource.saveWeather(weather);
        }
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


    private void refreshWeather(LoadWeatherCallback callback, final List<Weather> originalList,
                        List<Weather> outdatedList) {
        int originalSize = originalList.size();
        for (Weather outdated : outdatedList) {
            deleteWeather(outdated.getId());
            originalList.remove(outdated);
            getWeather(outdated.getCity(), new FetchWeatherCallback() {
                @Override
                public void onWeatherFetched(Weather weather) {
                   originalList.add(weather);
                }

                @Override
                public void onDataNotFetched() {
                }
            });
        }

        if (originalList.size() == originalSize) {
            callback.onWeatherLoaded(originalList);
        }
    }

}
