package com.shopback.nardweather;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.support.v7.widget.RecyclerView;

import java.util.LinkedList;


public class WeatherActivity extends AppCompatActivity {
    public static final int INVALID_CITY = 3;

    public static Handler postToUiHandler;
    NetworkReceiver networkReceiver = NetworkReceiver.getInstance();
    IntentFilter filter = new IntentFilter();

    WeatherManager weatherManager;
    LinkedList<WeatherResults> weatherList;
    WeatherAdapter weatherAdapter;
    static Dialog dialogDisplayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_recycler);

        if (!WeatherManager.hasInstance) {
            weatherManager = WeatherManager.getInstance(WeatherActivity.this);
        } else {
            weatherManager = WeatherManager.getInstance();
        }
        postToUiHandler = WeatherManager.getInstance().getMainThreadHandler();

        //setting up recycler view
        RecyclerView weatherRecyclerView = findViewById(R.id.weather_recycler_view);
        weatherRecyclerView.setHasFixedSize(true);
        //initialise weather list
        weatherList = new LinkedList<>();
        //connecting to layout manager
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //creating of adapter and link to recycler view
        weatherAdapter = new WeatherAdapter(this, weatherList);
        weatherRecyclerView.setAdapter(weatherAdapter);


    }

    @Override
    public void onStart() {
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
        super.onStart();
    }

    @Override
    public void onStop() {
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
        if (item.getItemId() == R.id.add_cities) {
            showInputDialog();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        NetworkInfo networkInfo = NetworkUtil.getActiveNetworkInfo(this);
        if (networkInfo != null && networkInfo.isConnected()) {
            menu.findItem(R.id.add_cities).setEnabled(true);
        } else {
            menu.findItem(R.id.add_cities).setEnabled(false);
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
     */
    private void updateUI() {
        //TODO: FILL THIS UP
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
            @Override
            public void run() {
                Log.d("Network", "fetching " + city);
                WeatherResults data = FetchWeather.getWeather(WeatherActivity.this, city);
                if (data != null) {
                    int currentSize = weatherAdapter.getItemCount();
                    weatherList.add(data);
                    postToUiHandler.post(getUiRunnable(currentSize));
                } else {
                    Log.d("Network", "no data fetched for" + city);
                }
            }
        };
    }

    /**
     * Creates a Runnable object to update the UI fields based on the WeatherResults.
     * Toast error message is shown if the city's weather information cannot be fetched
     *
     * @param pos: int
     * @return Runnable
     */
    private Runnable getUiRunnable(final int pos) {
        return new Runnable() {
            public void run() {
                weatherAdapter.notifyDataSetChanged();
            }
        };
    }

    public static void showOfflineDialog(Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.offline_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.BOTTOM);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        Button cancelButton = dialog.findViewById(R.id.offline_dialog_cancel_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        setDialog(dialog);
    }

    public static void dismissDialg() {
        if (dialogDisplayed != null) {
            dialogDisplayed.dismiss();
        }
    }

    private static void setDialog(Dialog dialog){
        dialogDisplayed = dialog;
    }


}
