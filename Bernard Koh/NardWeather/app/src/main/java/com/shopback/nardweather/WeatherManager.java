package com.shopback.nardweather;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class WeatherManager {

    private static final int KEEP_ALIVE_TIME = 1;
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAXIMUM_POOL_SIZE = 3;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private final ThreadPoolExecutor fetchWeatherJobs;

    //queue of Runnables for fetching weather information
    private final BlockingQueue<Runnable> fetchWeatherQueue;

    private Handler handler;

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
        fetchWeatherQueue = new LinkedBlockingQueue<Runnable>();
        fetchWeatherJobs = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, fetchWeatherQueue);

        handler = new Handler(Looper.getMainLooper());
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

    Handler getMainThreadHandler() {
        return handler;
    }

}
