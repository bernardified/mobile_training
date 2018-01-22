package com.shopback.summation;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by bernardkoh on 19/1/18.
 */

public class CalculationThreadPool {
    private static CalculationThreadPool mInstance;
    private static ThreadPoolExecutor mThreadPoolExecutor;
    private static int MAX_POOL_SIZE;
    private static final int KEEP_ALIVE = 10;
    BlockingQueue<Runnable> calculationQueue = new LinkedBlockingQueue<>();


    private CalculationThreadPool() {
        int coreNum = Runtime.getRuntime().availableProcessors();
        MAX_POOL_SIZE = 10;
        mThreadPoolExecutor = new ThreadPoolExecutor(coreNum, MAX_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, calculationQueue);
    }

    public static synchronized void post(Runnable runnable) {
        if(mInstance == null) {
            mInstance = new CalculationThreadPool();
        }
        mInstance.mThreadPoolExecutor.execute(runnable);
    }

}
