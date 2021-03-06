package com.shopback.speeddial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView nameView, numberView, indicatorView;
    private Button assignButton;

    private static int lastPressedNumber;

    private static final int CHOOSE_CONTACT_CODE = 1234;
    private static final String SPEED_DIAL_PREF = "Speed Dial";
    final static String permissionToCall = Manifest.permission.CALL_PHONE;

    private SharedPreferences speedDialPref;
    public static ArrayList<Contact> savedContacts;
    private SharedPreferences.Editor dataEditor;
    private Gson gson;

    public static MainActivity mainActivity;

    static MainActivity getMainActivity() {
        return mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUi();
        loadSavedData();
        mainActivity = this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contact_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        clearContact();
        return true;
    }


    public void chooseContact(View v) {
        Intent chooseContactIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(chooseContactIntent, CHOOSE_CONTACT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_CONTACT_CODE:
                    readContact(data, lastPressedNumber);
            }
        }
    }

    private void readContact(Intent data, int buttonPresed) {
        Contact contact;
        Uri contactDetails = data.getData();
        Cursor cursor = getContentResolver().query(contactDetails, null, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                int nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                contact = new Contact(cursor.getString(nameColumn), cursor.getString(numberColumn));
                nameView.setText(contact.getName());
                numberView.setText(contact.getNumber());
                savedContacts.set(buttonPresed, contact);
                saveData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.assign_contact_button:
                chooseContact(view);
                break;
            default:
                assignButton.setEnabled(true);
                changeContactView(((Button) view).getText().toString());
        }
    }

    private void initializeUi() {
        Button num2, num3, num4, num5, num6, num7, num8, num9;

        setContentView(R.layout.activity_main);
        nameView = findViewById(R.id.name);
        numberView = findViewById(R.id.contact_number);
        indicatorView = findViewById(R.id.number_indicator);

        num2 = findViewById(R.id.button_num_2);
        num2.setOnClickListener(this);
        num3 = findViewById(R.id.button_num_3);
        num3.setOnClickListener(this);
        num4 = findViewById(R.id.button_num_4);
        num4.setOnClickListener(this);
        num5 = findViewById(R.id.button_num_5);
        num5.setOnClickListener(this);
        num6 = findViewById(R.id.button_num_6);
        num6.setOnClickListener(this);
        num7 = findViewById(R.id.button_num_7);
        num7.setOnClickListener(this);
        num8 = findViewById(R.id.button_num_8);
        num8.setOnClickListener(this);
        num9 = findViewById(R.id.button_num_9);
        num9.setOnClickListener(this);
        assignButton = findViewById(R.id.assign_contact_button);
        assignButton.setOnClickListener(this);

        gson = new Gson();
    }

    private void loadSavedData() {
        new Thread() {
            public void run() {
                Log.d("Initialization", "loading data");
                speedDialPref = getSharedPreferences(SPEED_DIAL_PREF, MODE_PRIVATE);
                dataEditor = speedDialPref.edit();
                String json = speedDialPref.getString(SPEED_DIAL_PREF, null);
                if (json != null) {
                    savedContacts = gson.fromJson(json, new TypeToken<ArrayList<Contact>>() {
                    }.getType());
                } else {
                    savedContacts = new ArrayList<>(10);
                    for (int i = 0; i < 10; i++) {
                        savedContacts.add(new Contact());
                    }
                    Log.d("Initialization", "Empty data loaded");
                }
                nameView.setText(R.string.lauch_view);
            }
        }.start();
    }

    private void saveData() {
        new Thread() {
            public void run() {
                String json = gson.toJson(savedContacts);
                dataEditor.putString(SPEED_DIAL_PREF, json);
                dataEditor.apply();
            }
        }.start();
    }

    private void changeContactView(String number) {
        Log.d("change view", number + " pressed");
        lastPressedNumber = Integer.valueOf(number);
        Contact contact = savedContacts.get(lastPressedNumber);
        if (contact.getName() != null) {
            nameView.setText(contact.getName());
            numberView.setText(contact.getNumber());
        } else {
            nameView.setText(R.string.no_contact_set);
            numberView.setText("");
        }
        indicatorView.setText(number);
    }

    private void clearContact() {
        savedContacts.set(lastPressedNumber, new Contact());
        nameView.setText(R.string.no_contact_set);
        numberView.setText("");
        saveData();
    }

    public void makePhoneCall(Context context, String input) {
        String numberToCall = null;
        try {

            if (ActivityCompat.checkSelfPermission(context, MainActivity.permissionToCall) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{MainActivity.permissionToCall}, 1);

            } else {
                Integer dialPressed = Integer.parseInt(input);
                if (dialPressed >= 2 && dialPressed <= 9) {
                    numberToCall = MainActivity.savedContacts.get(dialPressed).getNumber();
                    Log.d("call receiver", "numberToCall = " + numberToCall);

                    //no contact registered for the number
                    if (numberToCall == null) {
                        Toast.makeText(context, "No speed dial set", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (NumberFormatException nfe) {
            numberToCall = input;
            Log.d("call receiver", input + " is not integer");
        } finally {

            if (numberToCall != null) {
                Intent makePhoneCallIntent = new Intent(Intent.ACTION_CALL);
                makePhoneCallIntent.setData(Uri.parse("tel:" + numberToCall));
                context.startActivity(makePhoneCallIntent);
            } else {
                Intent makePhoneCallIntent = new Intent(Intent.ACTION_DIAL);
                makePhoneCallIntent.setData(Uri.parse("tel:" + input));
                context.startActivity(makePhoneCallIntent);
            }
        }
    }
}

