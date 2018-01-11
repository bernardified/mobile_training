package com.shopback.nardweather;

import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;


public class WeatherActivity extends AppCompatActivity {

    private static final String DEFAULT_CITY_SG = "Singapore,SG";
    private static final String DEFAULT_CITY_MY = "Johor Bahru , MY";
    private static final String DEFAULT_CITY_ID = "Jakarta,ID";
    private static final String WEATHER_FONT_PATH = "fonts/weathericons_regular_webfont.ttf";

    private static int count = 0;

    public static Handler postToUiHandler;
    NetworkReceiver networkReceiver = NetworkReceiver.getInstance();
    IntentFilter filter = new IntentFilter();

    WeatherManager weatherManager;
    LinkedList<WeatherResults> dataSet;

    private TextView cityField, lastUpdatedField, weatherIcon, temperatureField,
            cityFieldTwo, lastUpdatedFieldTwo, weatherIconTwo, temperatureFieldTwo,
            cityFieldThree, lastUpdatedFieldThree, weatherIconThree, temperatureFieldThree;
 /*   private RecyclerView weatherRecyclerView;
    private RecyclerView.Adapter weatherAdapter;
    private RecyclerView.LayoutManager weatherLayoutManager;*/


    Typeface weatherFont;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_multi);

        if (!WeatherManager.hasInstance) {
            weatherManager = WeatherManager.getInstance(WeatherActivity.this);
        } else {
            weatherManager = WeatherManager.getInstance();
        }
        postToUiHandler = WeatherManager.getInstance().getMainThreadHandler();
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
/*
        //setting up recycler view
        weatherRecyclerView = findViewById(R.id.weather_recycler_view);
        weatherRecyclerView.setHasFixedSize(true);
        //connecting to layout manager
        weatherLayoutManager = new LinearLayoutManager(this);
        weatherRecyclerView.setLayoutManager(weatherLayoutManager);
        //creating of adapter and link to recycler view
        weatherAdapter = new WeatherAdapter(dataSet);
        weatherRecyclerView.setAdapter(weatherAdapter); */
    }

    @Override
    public void onStart() {
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
        super.onStart();
    }

    @Override
    public  void onStop() {
        unregisterReceiver(networkReceiver);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        //to update from saved preference next time
        updateWeather(DEFAULT_CITY_SG, DEFAULT_CITY_MY, DEFAULT_CITY_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.weather, menu);
        return true;
    }

    /**
     * Invokes input Alert Dialog when change cities is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.change_cities) {
            showInputDialog();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        NetworkInfo networkInfo = NetworkUtil.getActiveNetworkInfo(this);
        if (networkInfo != null && networkInfo.isConnected()) {
            menu.findItem(R.id.change_cities).setEnabled(true);
        } else {
           menu.findItem(R.id.change_cities).setEnabled(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    /**
     * shows the input Alert Dialog with 3 edit text field  and two buttons for user to enter city name
     */
    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        builder.setTitle("Search by City");
        builder.setMessage("Please enter cities:");

        final EditText inputOne = new EditText(this);
        inputOne.setInputType(InputType.TYPE_CLASS_TEXT);
        inputOne.setHint(R.string.city_name_example);
        inputOne.setHintTextColor(Color.GRAY);
        layout.addView(inputOne);

        final EditText inputTwo = new EditText(this);
        inputTwo.setInputType(InputType.TYPE_CLASS_TEXT);
        inputTwo.setHint(R.string.city_name_example_two);
        inputTwo.setHintTextColor(Color.GRAY);
        layout.addView(inputTwo);

        final EditText inputThree = new EditText(this);
        inputThree.setInputType(InputType.TYPE_CLASS_TEXT);
        inputThree.setHint(R.string.city_name_example_three);
        inputThree.setHintTextColor(Color.GRAY);
        layout.addView(inputThree);

        builder.setView(layout);

        builder.setPositiveButton(R.string.change_city_dialog_go, new DialogInterface.OnClickListener() {
            @Override
            //change city when Go Button pressed
            public void onClick(DialogInterface dialog, int id) {
                updateWeather(inputOne.getText().toString(), inputTwo.getText().toString(),
                        inputThree.getText().toString());
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

    /**
     * Fetches the weather info of the three input cities
     *
     * @param city1: String
     * @param city2: String
     * @param city3: String
     */
    private void updateWeather(final String city1, final String city2, final String city3) {
        weatherManager.getFetchWeatherJobs().execute(getFetchWeatherRunnable(city1));
        weatherManager.getFetchWeatherJobs().execute(getFetchWeatherRunnable(city2));
        weatherManager.getFetchWeatherJobs().execute(getFetchWeatherRunnable(city3));
    }

    /**
     * Dynamically retrieves respective weather icon from strings.xml and updates UI into their respective views
     *
     * @param data: WeatherResults
     */
    private void updateUI(WeatherResults data) {
        int weatherIconIdentifier;
        switch (getOrder()) {

            case 0:
                Log.d("updateUI", "updating UI 1");
                cityField.setText(data.getCity());
                lastUpdatedField.setText(data.getLastUpdated());
                temperatureField.setText(data.getTemperature());

                //get the id of the respective weather icon based on the weather code
                weatherIconIdentifier = getResources().getIdentifier(data.getWeatherIcon(),
                        "string", this.getPackageName());

                Log.d("updateUI", data.getWeatherIcon());

                if (weatherIconIdentifier == 0) {
                    weatherIcon.setText("");
                } else {
                    weatherIcon.setText(weatherIconIdentifier);
                }
                break;

            case 1:

                Log.d("updateUI", "updating UI 2");
                cityFieldTwo.setText(data.getCity());
                lastUpdatedFieldTwo.setText(data.getLastUpdated());
                temperatureFieldTwo.setText(data.getTemperature());

                //get the id of the respective weather icon based on the weather code
                weatherIconIdentifier = getResources().getIdentifier(data.getWeatherIcon(),
                        "string", this.getPackageName());

                Log.d("updateUI", data.getWeatherIcon());
                if (weatherIconIdentifier == 0) {
                    weatherIconTwo.setText("");
                } else {
                    weatherIconTwo.setText(weatherIconIdentifier);
                }
                break;

            case 2:
                Log.d("updateUI", "updating UI 3");
                cityFieldThree.setText(data.getCity());
                lastUpdatedFieldThree.setText(data.getLastUpdated());
                temperatureFieldThree.setText(data.getTemperature());
                //get the id of the respective weather icon based on the weather code
                weatherIconIdentifier = getResources().getIdentifier(data.getWeatherIcon(),
                        "string", this.getPackageName());

                Log.d("updateUI", data.getWeatherIcon());
                if (weatherIconIdentifier == 0) {
                    weatherIconThree.setText("");
                } else {
                    weatherIconThree.setText(weatherIconIdentifier);
                }
                break;
        }

    }

    /**
     * Creates a Runnable object to fetch weather information and post the result to the main thread
     * to update UI
     *
     * @param city: String
     * @return Runnable
     */
    private Runnable getFetchWeatherRunnable(final String city) {
        return new Runnable() {
            Runnable runData;
            WeatherResults results;

            @Override
            public void run() {
                try {
                    Log.d("Network", "fetching "+city);
                    results = FetchWeather.getWeather(WeatherActivity.this, city);
                    runData = getUiRunnable(results);
                    if (runData != null) {
                        postToUiHandler.post(getUiRunnable(results));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Creates a Runnable object to update the UI fields based on the WeatherResults.
     * Toast error message is shown if the city's weather information cannot be fetched
     *
     * @param results: WeatherResults
     * @return Runnable
     */
    private Runnable getUiRunnable(final WeatherResults results) {
        if (results == null) {
            Log.d("updateWeather", "data is null");
        } else {
            return new Runnable() {
                public void run() {
                    updateUI(results);
                }
            };
        }
        return null;
    }

    private static Integer getOrder() {
        return count++ % 3;
    }
}
