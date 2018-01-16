package com.shopback.nardweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {

    private static int networkStatus;
    private static boolean hasInstance = false;
    private static NetworkReceiver receiver;


    private NetworkReceiver(){
        hasInstance = true;
    }

    /**
     * Detect changes in the network and sends a message which is displayed on the main activity
     * @param context: Context
     * @param intent: Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Message message = new Message();
        Bundle b = new Bundle();

        NetworkInfo info = NetworkUtil.getActiveNetworkInfo(context);

        if (info == null || !info.isConnectedOrConnecting() ) {
            Log.d("Network", "disconnected");
            message.what = NetworkUtil.NETWORK_ERROR_ID;
            networkStatus = NetworkUtil.NETWORK_ERROR_ID;
        } else {
            Log.d("Network", "connected");
            message.what = NetworkUtil.NETWORK_NO_ERROR_ID;
            networkStatus = NetworkUtil.NETWORK_NO_ERROR_ID;
            b.putString("errorMessage", "Connected to Internet");
        }
        message.setData(b);
        WeatherManager.getInstance().getMainThreadHandler().sendMessage(message);
    }

    public static NetworkReceiver getInstance() {
        if(!hasInstance) {
            receiver = new NetworkReceiver();
        }
        return receiver;
    }

    public static int getNetworkStatus() {
        return networkStatus;
    }

    public static void setNetworkStatus(int networkStatus) {
        NetworkReceiver.networkStatus = networkStatus;
    }


}


