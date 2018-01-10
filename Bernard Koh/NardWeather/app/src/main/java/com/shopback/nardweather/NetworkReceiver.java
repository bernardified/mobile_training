package com.shopback.nardweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Message status = new Message();
        Bundle b = new Bundle();
        boolean hasInternet = false;
        try {
            hasInternet = NetworkUtil.getActiveNetworkInfo(context).isConnected();
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
        if (hasInternet) {
            status.what = NetworkUtil.NETWORK_NO_ERROR_ID;
            b.putString("errorMessage", "Connected to Internet");
        } else {
            status.what = NetworkUtil.NETWORK_ERROR_ID;
            b.putString("errorMessage", "No Internet Connection available");
        }
        status.setData(b);
        WeatherManager.getInstance().getMainThreadHandler().sendMessage(status);
    }
}


