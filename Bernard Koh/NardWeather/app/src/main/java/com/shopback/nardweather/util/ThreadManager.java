package com.shopback.nardweather.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.shopback.nardweather.NoArchitecture.WeatherActivity;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {

    private final ThreadPoolExecutor networkJobs;
    private final ThreadPoolExecutor diskJobs;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    //single instance of WeatherManager. Singleton pattern
    private static ThreadManager threadManagerInstance = null;

    private String errorMessage;
    private final Executor mainThread;


    /**
     * Private ThreadManager constructor
     *
     * @param context
     */
    private ThreadManager(final Context context) {

        //initialize thread pool to run multiple fetch weather jobs
        BlockingQueue<Runnable> networkQueue = new LinkedBlockingQueue<Runnable>();
        networkJobs = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, networkQueue);

        BlockingQueue<Runnable> diskQueue = new LinkedBlockingQueue<Runnable>();
        diskJobs = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, diskQueue);

        mainThread = new MainThreadExecutor();

    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
            /*@Override
            public void handleMessage(Message inputMessage) {
                Bundle b = inputMessage.getData();
                errorMessage = b.getString(Util.ERROR_MESSAGE_KEY);

                if(inputMessage.what == Util.NETWORK_ERROR_ID) {
                    WeatherActivity.showOfflineDialog(activity);
                } else if (inputMessage.what == Util.NETWORK_NO_ERROR_ID) {
                    WeatherActivity.dismissDialog();
                } else {
                    Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
                }

                if (inputMessage.what == Util.NETWORK_ERROR_ID ||
                        inputMessage.what == Util.NETWORK_NO_ERROR_ID) {
                    activity.invalidateOptionsMenu();
                }
            }*/

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

    /**
     * Singleton Pattern
     *
     * @return ThreadManager
     */

    public static ThreadManager getInstance(Context context) {
        if (threadManagerInstance == null) {
            synchronized (ThreadManager.class) {
                if (threadManagerInstance == null) {
                    threadManagerInstance = new ThreadManager(context);
                }
            }
        }
        return threadManagerInstance;
    }

    public ThreadPoolExecutor getNetworkThreadPool() {
        return networkJobs;
    }
    public ThreadPoolExecutor getDiskThreadPool() {
        return diskJobs;
    }

    public Executor mainThread() {
        return mainThread;
    }
}
