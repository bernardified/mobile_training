package com.shopback.nardweather;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WeatherManager {

    private static final int KEEP_ALIVE_TIME = 1;
    private static final int CORE_POOL_SIZE = 6;
    private static final int MAXIMUM_POOL_SIZE = 6;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private final ThreadPoolExecutor fetchWeatherJobs;
    private final ThreadPoolExecutor parseWeatherJobs;

    //queue of Runnables for fetching weather information
    private final BlockingQueue<Runnable> fetchWeatherQueue;

    //queue of Runnable for parsing weather info
    private final BlockingQueue<Runnable> parseWeatherQueue;

    private Handler handler;

    //single instance of WeatherManager. Singleton pattern
    private static WeatherManager sInstance = null;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new WeatherManager();
    }

    private WeatherManager() {
        fetchWeatherQueue = new LinkedBlockingQueue<Runnable>();
        parseWeatherQueue = new LinkedBlockingQueue<Runnable>();
        fetchWeatherJobs = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, fetchWeatherQueue);
        parseWeatherJobs = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, parseWeatherQueue);

        handler = new Handler(Looper.getMainLooper());
    }

    public static WeatherManager getInstance() {
        if (sInstance == null) {
            synchronized (WeatherManager.class) {
                sInstance = new WeatherManager();
            }
        }
        return sInstance;
    }

    public ThreadPoolExecutor getFetchWeatherJobs() {
        return fetchWeatherJobs;
    }

    public ThreadPoolExecutor getParseWeatherJobs() {
        return parseWeatherJobs;
    }

    public Handler getUIJobs() {
        return handler;
    }




}
