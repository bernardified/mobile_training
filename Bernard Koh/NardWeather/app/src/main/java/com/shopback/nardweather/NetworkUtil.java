package com.shopback.nardweather;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;



class NetworkUtil{
    static final int NETWORK_ERROR_ID = 1;
    static final int NETWORK_NO_ERROR_ID = 2;
    static final int NETWORK_SLOW = 5;
    private static ConnectivityManager cm;

    /**
     * Retrieves the current network status information
     * @param context: Context
     * @return NetworkInfo
     */
    static NetworkInfo getActiveNetworkInfo(Context context) {
        if (cm == null) {
            cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo;
        }

        return null;
    }
}
