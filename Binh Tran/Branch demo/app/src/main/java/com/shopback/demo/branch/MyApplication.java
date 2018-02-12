package com.shopback.demo.branch;

import android.app.Application;

import io.branch.referral.Branch;

/**
 * Created by Binh Tran on 12/2/18.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the Branch object
        Branch.getAutoInstance(this);
    }
}
