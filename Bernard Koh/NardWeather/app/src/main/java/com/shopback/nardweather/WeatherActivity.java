package com.shopback.nardweather;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private static final String WEATHER_FONT_PATH = "fonts/weathericons_regular_webfont.ttf";

    Handler postToUiHandler;
    TextView cityField, lastUpdatedField, weatherIcon, temperatureField,
            cityFieldTwo, lastUpdatedFieldTwo, weatherIconTwo, temperatureFieldTwo,
            cityFieldThree, lastUpdatedFieldThree, weatherIconThree, temperatureFieldThree;

    Typeface weatherFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_multi);

        weatherFont = Typeface.createFromAsset(this.getAssets(), WEATHER_FONT_PATH);

        cityField = findViewById(R.id.city_field);
        lastUpdatedField = findViewById(R.id.updated_field);
        weatherIcon = findViewById(R.id.weather_icon);
        temperatureField = findViewById(R.id.current_temperature_field);
        weatherIcon.setTypeface(weatherFont);

        cityFieldTwo = findViewById(R.id.city_field_2);
        lastUpdatedFieldTwo = findViewById(R.id.updated_field_2);
        weatherIconTwo = findViewById(R.id.weather_icon_2);
        temperatureFieldTwo = findViewById(R.id.current_temperature_field_2);
        weatherIconTwo.setTypeface(weatherFont);

        cityFieldThree = findViewById(R.id.city_field_3);
        lastUpdatedFieldThree = findViewById(R.id.updated_field_3);
        weatherIconThree = findViewById(R.id.weather_icon_3);
        temperatureFieldThree = findViewById(R.id.current_temperature_field_3);
        weatherIconThree.setTypeface(weatherFont);

        if (savedInstanceState == null) {
            updateWeather(DEFAULT_CITY);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    /*Invokes input Alert Dialog when change city is selected*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_city) {
            showInputDialog();
        }
        return false;
    }

    /*shows the input Alert Dialog with edit text and two buttons for user to enter city name*/
    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search by City");
        builder.setMessage("Please enter city:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.city_name_example);
        input.setHintTextColor(Color.GRAY);
        builder.setView(input);
        builder.setPositiveButton(R.string.change_city_dialog_go, new DialogInterface.OnClickListener() {
            @Override
            //change city when Go Button pressed
            public void onClick(DialogInterface dialog, int id) {
                changeCity(input.getText().toString());
            }
        }).setNegativeButton(R.string.change_city_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            //do nothing when Cancel Button pressed
            public void onClick(DialogInterface dialogInterface, int id) {
                //do nothing
            }
        });
        builder.show();
    }

    public void changeCity(String city) {
        updateWeather(city);
    }

    /*Create a new thread to retrieve weather information
    * and posts to UI thread to update UI
    * Error message shown when there is an invalid entry
    **/
    private void updateWeather(final String city) {
        new Thread() {
            public void run() {
                final JSONObject data = doFetchWeather(city);
                final WeatherResults results = FetchWeather.parseResult(data);

                if(results == null) {
                    Log.d("updateWeather", "data is null");
                    Runnable noData = new Runnable(){
                        public void run() {
                            Toast.makeText(WeatherActivity.this, "Unable to find city!", Toast.LENGTH_LONG)
                                    .show();
                        }};
                    postToUiHandler.post(noData);

                } else {
                    Log.d("updateWeather", "data parsing");
                    Runnable parsedData = new Runnable() {
                        public void run() {
                            updateUI(results);
                        }};
                    postToUiHandler.post(parsedData);
                }
            }

        }.start();
    }

    /*Dynamically retrieves respective weather icon from strings.xml and updates UI*/
    private void updateUI(WeatherResults data) {
        Log.d("updateUI", "updating UI");
        cityField.setText(data.getCity());
        lastUpdatedField.setText(data.getLastUpdated());
        temperatureField.setText(data.getTemperature());

        //get the id of the respective weather icon based on the weather code
        int weatherIconIdentifier = getResources().getIdentifier(data.getWeatherIcon(),
                "string", this.getPackageName());

        Log.d("updateUI", data.getWeatherIcon());

        String weather;
        if (weatherIconIdentifier != 0) {
            weather = getString(weatherIconIdentifier);
        } else {
            Log.d("updateUI", "weatherIcon is null");
            weather = null;
        }

        if (weatherIcon != null) {
            weatherIcon.setText(weather);
        }

    }

    private JSONObject doFetchWeather(String city) {
        WeatherManager.getInstance().getFetchWeatherJobs().execute(new Runnable() {
            @Override
            public void run() {
                return FetchWeather.getJSON(WeatherActivity.this,city);
            }
        });
        return null;
    }

    private void doParseWeather(JSONObject data) {
        WeatherManager.getInstance().getParseWeatherJobs().execute(new);
    }

}
