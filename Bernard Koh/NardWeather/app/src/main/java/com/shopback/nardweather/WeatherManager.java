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

    private final ThreadPoolExecutor fetchWeatherJobs;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    //single instance of WeatherManager. Singleton pattern
    private static WeatherManager sInstance = null;
    static boolean hasInstance = false;

    private Handler mainThreadHandler;
    private String errorMessage;


    /**
     * Private WeatherManager constructor
     * @param activity: Activity
     */
    private WeatherManager(final Activity activity) {

        //initialize thread pool to run multiple fetch weather jobs
        BlockingQueue<Runnable> fetchWeatherQueue = new LinkedBlockingQueue<>();
        fetchWeatherJobs = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, fetchWeatherQueue);

        mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                Bundle b = inputMessage.getData();
                errorMessage = b.getString("errorMessage");
                if(inputMessage.what == NetworkUtil.NETWORK_ERROR_ID) {
                    WeatherActivity.showOfflineDialog(activity);

                } else if (inputMessage.what == WeatherActivity.INVALID_CITY) {
                    Toast.makeText(activity, errorMessage + " not found!", Toast.LENGTH_SHORT).show();

                } else if (inputMessage.what == NetworkUtil.NETWORK_NO_ERROR_ID){
                    WeatherActivity.dismissDialog();
                   // Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                } else if (inputMessage.what == WeatherActivity.DUPLICATE_CITY) {
                    Toast.makeText(activity, errorMessage + " already exists!", Toast.LENGTH_SHORT).show();
                }

                if (inputMessage.what == NetworkUtil.NETWORK_ERROR_ID ||
                        inputMessage.what == NetworkUtil.NETWORK_NO_ERROR_ID) {
                    activity.invalidateOptionsMenu();
                }

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
