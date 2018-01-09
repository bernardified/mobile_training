package com.shopback.nardweather;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class WeatherManager {

    private static final int KEEP_ALIVE_TIME = 1;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private final ThreadPoolExecutor fetchWeatherJobs;

    //single instance of WeatherManager. Singleton pattern
    private static WeatherManager sInstance = null;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new WeatherManager();
    }

    /**Private WeatherManager constructor
     *
     */
    private WeatherManager() {
        BlockingQueue<Runnable> fetchWeatherQueue = new LinkedBlockingQueue<>();
        fetchWeatherJobs = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, fetchWeatherQueue);
    }

    /**Singleton Pattern
     *
     * @return WeatherManager
     */
    static WeatherManager getInstance() {
        if (sInstance == null) {
            synchronized (WeatherManager.class) {
                sInstance = new WeatherManager();
            }
        }
        return sInstance;
    }

    ThreadPoolExecutor getFetchWeatherJobs() {
        return fetchWeatherJobs;
    }

}
