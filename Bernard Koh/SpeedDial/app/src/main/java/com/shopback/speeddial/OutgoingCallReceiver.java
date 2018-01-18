package com.shopback.speeddial;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class OutgoingCallReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d("call receiver", phoneNumber + " pressed");
        makePhoneCall(context, phoneNumber);
    }


    public void makePhoneCall(Context context, String input) {
        String numberToCall = null;
        try {
            Integer dialPressed = Integer.parseInt(input);
            if (dialPressed >= 2 && dialPressed <= 9) {
                numberToCall = MainActivity.savedContacts.get(dialPressed).getNumber();
                Log.d("call receiver", "numberToCall = " +numberToCall);

                //no contact registered for the number
                if (numberToCall == null) {
                    Toast.makeText(context, "No speed dial set", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NumberFormatException nfe) {
                numberToCall = input;
                Log.d("call receiver", input + " is not integer");
        } finally {
            if (numberToCall != null) {
                Intent makePhoneCallIntent = new Intent(Intent.ACTION_CALL);
                makePhoneCallIntent.setData(Uri.parse("tel:"+ numberToCall));
                context.startActivity(makePhoneCallIntent);
                Log.d("call receiver", "calling " + input);
            } else {
                Intent makePhoneCallIntent = new Intent(Intent.ACTION_DIAL);
                makePhoneCallIntent.setData(Uri.parse("tel:"+ input));
                context.startActivity(makePhoneCallIntent);
                Log.d("call receiver", "dialling " + input);
            }
        }
    }
}