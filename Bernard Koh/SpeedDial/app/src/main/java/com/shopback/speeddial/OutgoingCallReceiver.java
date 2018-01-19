package com.shopback.speeddial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OutgoingCallReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d("call receiver", phoneNumber + " pressed");
        MainActivity.getMainActivity().makePhoneCall(context, phoneNumber);
    }
}