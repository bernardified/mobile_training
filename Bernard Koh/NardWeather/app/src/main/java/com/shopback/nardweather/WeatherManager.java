package com.shopback.nardweather;

import android.app.Activity;
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
    private WeatherManager(final Activity activity) {
        BlockingQueue<Runnable> fetchWeatherQueue = new LinkedBlockingQueue<>();
        fetchWeatherJobs = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, fetchWeatherQueue);

        mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {

                if(inputMessage.what == NetworkUtil.NETWORK_ERROR_ID) {
                    WeatherActivity.hasInternetConnection = false;
                    activity.invalidateOptionsMenu();
                }

                Bundle b = inputMessage.getData();
                errorMessage = b.getString("errorMessage");
                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();


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

    static WeatherManager getInstance(Activity activity) {
        synchronized (WeatherManager.class) {
            sInstance = new WeatherManager(activity);
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
