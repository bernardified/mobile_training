package com.shopback.nardweather;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class WeatherManager {

    static boolean hasInstance = false;

    private final Context context;

    private static final int KEEP_ALIVE_TIME = 1;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private final ThreadPoolExecutor fetchWeatherJobs;

    //single instance of WeatherManager. Singleton pattern
    private static WeatherManager sInstance = null;

    private Handler mainThreadHandler;

    private String errorMessage;


    /**
     * Private WeatherManager constructor
     */
    private WeatherManager(final Context context) {
        BlockingQueue<Runnable> fetchWeatherQueue = new LinkedBlockingQueue<>();
        fetchWeatherJobs = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, fetchWeatherQueue);
        this.context = context;

        mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Bundle b = inputMessage.getData();
                errorMessage = b.getString("errorMessage");
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();

            }
        };
    }

    /**
     * Singleton Pattern
     *
     * @return WeatherManager
     */

    static WeatherManager getInstance() {
        return sInstance;
    }

    static WeatherManager getInstance(Context context) {
        synchronized (WeatherManager.class) {
            sInstance = new WeatherManager(context);
            hasInstance = true;
        }
        return sInstance;
    }

    ThreadPoolExecutor getFetchWeatherJobs() {
        return fetchWeatherJobs;
    }

    Handler getMainThreadHandler() {
        return mainThreadHandler;
    }

}
