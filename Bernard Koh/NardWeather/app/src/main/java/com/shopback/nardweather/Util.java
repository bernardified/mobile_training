package com.shopback.nardweather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;

class Util {

    static String ERROR_MESSAGE_KEY = "errorMessage";

    static String CONNECTION_TIMEOUT_MESSAGE = "Connection Timeout. Retry again later.";
    static String INVALID_CITY_MESSAGE = "'s information cannot be found";
    static String DUPLICATE_CITY_MESSAGE = " already exists!";
    static String NETWORK_DISCONNECTED_MESSAGE = "Internet not Connected";
    static String NETWORK_CONNECTED_MESSAGE = "Connected to Internet";

    static final int NETWORK_ERROR_ID = 1;
    static final int NETWORK_NO_ERROR_ID = 2;
    static final int INVALID_CITY = 3;
    static final int DUPLICATE_CITY = 4;
    static final int NETWORK_SLOW = 5;
    static final int NETWORK_TIMEOUT = 6;


    static Message generateMessage(String key, int messageType, String inputMessage) {
        Message message = new Message();
        Bundle b = new Bundle();

        switch (messageType) {
            case NETWORK_TIMEOUT:
                message.what = NETWORK_TIMEOUT;
                break;
            case INVALID_CITY:
                message.what = INVALID_CITY;
                break;
            case NETWORK_ERROR_ID:
                message.what = NETWORK_ERROR_ID;
                break;
            case NETWORK_NO_ERROR_ID:
                message.what = NETWORK_NO_ERROR_ID;
                break;
            case DUPLICATE_CITY:
                message.what = DUPLICATE_CITY;
                break;
        }
        b.putString(key , inputMessage);
        message.setData(b);
        return message;
    }

    /**
     * Retrieves the current network status information
     * @param context: Context
     * @return NetworkInfo
     */
    static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo;
        }

        return null;
    }



}
