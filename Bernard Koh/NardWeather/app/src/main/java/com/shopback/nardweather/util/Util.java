package com.shopback.nardweather.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;

public class Util {

    public static String ERROR_MESSAGE_KEY = "errorMessage";

    public static String CONNECTION_TIMEOUT_MESSAGE = "Connection Timeout. Retry again later.";
    public static String INVALID_CITY_MESSAGE = "'s information cannot be found";
    public static String DUPLICATE_CITY_MESSAGE = " already exists!";
    public static String NETWORK_DISCONNECTED_MESSAGE = "Internet not Connected";
    public static String NETWORK_CONNECTED_MESSAGE = "Connected to Internet";

    public static final int NETWORK_ERROR_ID = 1;
    public static final int NETWORK_NO_ERROR_ID = 2;
    public static final int INVALID_CITY = 3;
    public static final int DUPLICATE_CITY = 4;
    public static final int NETWORK_SLOW = 5;
    public static final int NETWORK_TIMEOUT = 6;


    public static Message generateMessage(String key, int messageType, String inputMessage) {
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
    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo;
        }

        return null;
    }



}
