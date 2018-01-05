package com.shopback.nardweather;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity {

    private static final String DEFAULT_CITY = "Singapore,SG";

    Handler handler;
    TextView cityField, lastUpdatedField, weatherIcon, detailsField, temperatureField;
    Typeface weatherFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weathericons_regular_webfont.ttf");

        cityField = findViewById(R.id.city_field);
        lastUpdatedField = findViewById(R.id.updated_field);
        weatherIcon = findViewById(R.id.weather_icon);
        detailsField = findViewById(R.id.details_field);
        temperatureField = findViewById(R.id.current_temperature_field);
        weatherIcon.setTypeface(weatherFont);

        if (savedInstanceState == null) {
            updateWeather(DEFAULT_CITY);
        }

        handler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_city) {
            showInputDialog();
        }
        return false;
    }

    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter A City");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    public void changeCity(String city) {
        updateWeather(city);
    }

    private void updateWeather(final String city) {
        new Thread() {
            public void run() {
                final JSONObject data = FetchWeather.getJSON(WeatherActivity.this,city);
                if(data == null) {
                    Log.d("updateWeather", "data is null");
                    handler.post(new Runnable(){
                       public void run() {
                           Toast.makeText(WeatherActivity.this, "City not Found!", Toast.LENGTH_LONG)
                                   .show();
                       }
                    });

                } else {
                    Log.d("updateWeather", "data parsing");
                    handler.post(new Runnable(){
                        public void run() {
                           final WeatherResults results = FetchWeather.parseResult(data);
                           updateUI(results);
                        }
                    });
                }
            }

        }.start();
    }

    private void updateUI(WeatherResults data) {
        cityField.setText(data.getCity());
        lastUpdatedField.setText(data.getLastUpdated());
        detailsField.setText(data.getDetails());
        temperatureField.setText(data.getTemperature());
        weatherIcon.setText(getString(R.string.wi_owm_day_210));
    }

}
