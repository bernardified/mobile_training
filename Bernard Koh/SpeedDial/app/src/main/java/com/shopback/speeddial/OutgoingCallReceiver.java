package com.shopback.speeddial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class OutgoingCallReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(OutgoingCallReceiver.class.getSimpleName(), phoneNumber + " pressed");
       // Toast.makeText(context, "Outgoing call catched to " + phoneNumber, Toast.LENGTH_LONG).show();

        //make phone call out
        makePhoneCall(context, phoneNumber);
    }


    public void makePhoneCall(Context context, String input) {
        String numberToCall = null;
        try {
            Integer dialPressed = Integer.parseInt(input);
            if (dialPressed >= 2 && dialPressed <= 9) {
                numberToCall = MainActivity.savedContacts.get(dialPressed).getNumber();
                //no contact registered for the number
                if (numberToCall == null) {
                    numberToCall = input;
                }
            } else {
                numberToCall = input;
            }
        } catch (NumberFormatException nfe) {
                numberToCall = input;
        } finally {
            if (numberToCall != null) {
                Intent makePhoneCallIntent = new Intent(Intent.ACTION_DIAL);
                makePhoneCallIntent.setData(Uri.parse("tel:"+ numberToCall));
                context.startActivity(makePhoneCallIntent);
            }
        }
    }
}