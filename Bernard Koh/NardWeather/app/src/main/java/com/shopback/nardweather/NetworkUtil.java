package com.shopback.nardweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;


public class NetworkUtil extends BroadcastReceiver {
    public static final int NETWORK_ERROR_ID = 1;
    private static ConnectivityManager cm;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NetworkUtil", "triggered");
        boolean isVisible = WeatherActivity.isActivityVisible();
        try {
            if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) && isVisible) {

                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if((networkInfo != null) && networkInfo.isConnected()) {
                    Toast.makeText(context, "Connected to Internet", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "No Internet Available", Toast.LENGTH_LONG).show();
                }

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        if (cm == null) {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        try {
            return cm.getActiveNetworkInfo();
        } catch (NullPointerException e) {
            return null;
        }
    }
}
