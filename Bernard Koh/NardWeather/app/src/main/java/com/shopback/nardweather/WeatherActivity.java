package com.shopback.nardweather;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;


public class WeatherActivity extends AppCompatActivity {

    public static final int INVALID_CITY = 3;
    public static final int DUPLICATE_CITY = 4;
    public static final String CITY_PREF = "City";
    public static final String WEATHER_PREF = "Weather";
    public static Handler postToUiHandler;

    static Dialog dialogDisplayed;
    TextView emptyTextView;

    NetworkReceiver networkReceiver;
    IntentFilter filter = new IntentFilter();

    WeatherManager weatherManager;  //managed thread pool to schedule jobs
    LinkedList<String> cityList;   //list of cities
    HashMap<String, WeatherResults> weatherCache = new HashMap<>();   //cache of weather results

    WeatherAdapter weatherAdapter;
    RecyclerView weatherRecyclerView;

    SharedPreferences cityPref;
    SharedPreferences weatherPref;
    SharedPreferences.Editor cityEditor;
    SharedPreferences.Editor weatherEditor;
    Gson gson; //used to serialize and deserialize weatherList into json string object

    SwipeRefreshLayout swipeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_recycler);

        emptyTextView = findViewById(R.id.weather_list_empty);

        if (!WeatherManager.hasInstance) {
            weatherManager = WeatherManager.getInstance(WeatherActivity.this);
        } else {
            weatherManager = WeatherManager.getInstance();
        }
        postToUiHandler = WeatherManager.getInstance().getMainThreadHandler();
        networkReceiver = NetworkReceiver.getInstance();
        gson = new Gson();

        //setting up recycler view
        weatherRecyclerView = findViewById(R.id.weather_recycler_view);
        weatherRecyclerView.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        weatherRecyclerView.addItemDecoration(itemDecoration);
        //connecting to layout manager
        cityList = loadCityPref();
        weatherCache = loadWeatherPref();
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //creating of adapter and link to recycler view
        weatherAdapter = new WeatherAdapter(this, cityList, weatherCache);
        weatherRecyclerView.setAdapter(weatherAdapter);
        //set ItemTouchHelper to delete item on recycle view
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createCallBack());
        itemTouchHelper.attachToRecyclerView(weatherRecyclerView);

        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWeather();
                weatherAdapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_red_dark);
    }

    /**
     * registers network receiver during onStart callback
     */
    @Override
    public void onStart() {
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
        Log.d("Network", "receiver registered");
        super.onStart();
    }

    /**
     * deregisters network register during onStop callback
     */
    @Override
    public void onStop() {
        unregisterReceiver(networkReceiver);
        Log.d("Network", "receiver unregistered");
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        emptyTextView.setVisibility(cityList.isEmpty() ? View.VISIBLE : View.INVISIBLE);
        NetworkInfo networkInfo = NetworkUtil.getActiveNetworkInfo(this);
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            refreshWeather();
        }
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
     * @param item: MenuItem
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_cities) {
            showInputDialog();
            return true;
        }
        return false;
    }

    //disable the add cities button if there is no internet connection. commented out for now
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
      /*  NetworkInfo networkInfo = NetworkUtil.getActiveNetworkInfo(this);
        if (networkInfo != null && networkInfo.isConnected()) {
            menu.findItem(R.id.add_cities).setEnabled(true);
        } else {
            menu.findItem(R.id.add_cities).setEnabled(false);
        }
        super.onPrepareOptionsMenu(menu);
      */
        return true;
    }

    /**
     * shows the input Alert Dialog with 3 edit text field  and two buttons for user to enter city name
     */
    private void showInputDialog() {

        //TODO: convert to layout xml format
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

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

        new AlertDialog.Builder(this).setTitle("Search by City")
                .setMessage("Please enter cities:").setView(layout)
                .setPositiveButton(R.string.dialog_go, new DialogInterface.OnClickListener() {
                    @Override
                    //change city when Go Button pressed and store city in SharedPreference
                    public void onClick(DialogInterface dialog, int id) {
                        updateWeather(new LinkedList<>(Arrays.asList(
                                inputOne.getText().toString(),
                                inputTwo.getText().toString(),
                                inputThree.getText().toString()))
                        );
                    }
                }).setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    //do nothing when Cancel Button pressed
                    public void onClick(DialogInterface dialogInterface, int id) {
                        //do nothing
                    }
                }).show();
    }

    /**
     * Fetches the weather info of the input cities
     *
     * @param inputList: List retrieved from UI input
     */
    private void updateWeather(LinkedList<String> inputList) {
        for (String city: inputList) {
            weatherManager.getFetchWeatherJobs().execute(getFetchWeatherRunnable(city));
        }
    }

    /**
     * Refreshes weather information and updates cache. Updates those cities that previously could not
     * retrieve weather information too
     */
    public void refreshWeather() {
        //finds cities with invalid weather information
        ListIterator<String> iterator = cityList.listIterator();
        LinkedList<String> refreshList = new LinkedList<>();
        while (iterator.hasNext()) {
            String city = iterator.next();
            WeatherResults results = weatherCache.get(city);
            if (results.getResultType() == WeatherResults.ResultType.OFFLINE) {
                refreshList.add(city);
            }
        }
        //TODO:: check difference bewteen last updated time and current time. update those that havent been updated for too long
        iterator = refreshList.listIterator();
        while (iterator.hasNext()) {
            weatherCache.remove(cityList.remove(iterator.next()));
        }
        Log.d("Refresh Weather", "refreshing list");
        updateWeather(refreshList);

    }

    private void refreshOfflineWeather() {

    }

    private void refreshOutdatedWeather() {

    }

    /**
     * Creates a Runnable object to fetch weather information, save in preference and
     * post the result to the main thread to update UI. Duplicated cities will not be fetched
     *
     * @param city: String
     * @return Runnable
     */
    private Runnable getFetchWeatherRunnable(final String city) {
        return new Runnable() {
            @Override
            public void run() {
                WeatherResults data = FetchWeather.getWeather(WeatherActivity.this, city);
                if (data != null && !isDuplicating(data.getCity())) {
                    String newCity = data.getCity();
                    cityList.add(newCity);
                    weatherCache.put(newCity,data);
                    saveList();
                    postToUiHandler.post(getUiRunnable());
                    Log.d("Fetch Weather", "adding " + newCity);
                } else if (data == null &&
                        !city.isEmpty() &&
                        !isDuplicating(city)) {
                    //save the user input for future fetch
                    data = new WeatherResults(city.toUpperCase(), "","","","");
                    data.setResulType(WeatherResults.ResultType.OFFLINE);
                    cityList.add(city);
                    weatherCache.put(city, data);
                    saveList();
                    Log.d("Network", "no data fetched for " + city);
                } else {
                    Log.d("Network", "data is duplicated");
                }
                postToUiHandler.post(getUiRunnable());
            }
        };
    }

    /**
     * Creates a Runnable object to update the UI fields based on the WeatherResults.
     * Toast error message is shown if the city's weather information cannot be fetched
     *
     * @return Runnable
     */
    private Runnable getUiRunnable() {
        return new Runnable() {
            public void run() {
                Log.d("Network", "updating UI");
                weatherAdapter.notifyDataSetChanged();
                weatherRecyclerView.scrollToPosition(weatherAdapter.getItemCount()-1);
                if (!cityList.isEmpty()) {
                    emptyTextView.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    /**
     * Iterates through the weather list and checks if the input city is already in the list
     * @param city: String
     * @return true if there is a duplicate else return false
     */
    private boolean isDuplicating(String city) {
        Message message;
        Bundle b;

        ListIterator<String> iterator = cityList.listIterator();
        String next;
        while (iterator.hasNext()) {
            next = iterator.next();
            if (next.equals(city)) {
                message = new Message();
                b = new Bundle();
                message.what = WeatherActivity.DUPLICATE_CITY;
                b.putString("errorMessage", next + " already exists!");
                message.setData(b);
                WeatherManager.getInstance().getMainThreadHandler().sendMessage(message);
                return true;
            }
        }
        Log.d("Fetch Weather", "Check duplicates for " + city);
        return false;
    }


    /**
     * Saves the current weather list to SharedPreference
     */
    private synchronized void saveList() {
        String jsonCity = gson.toJson(cityList);
        String jsonWeather = gson.toJson(weatherCache);

        cityEditor.putString(CITY_PREF, jsonCity);
        weatherEditor.putString(WEATHER_PREF, jsonWeather);

        cityEditor.apply();
        weatherEditor.apply();
    }

    /**
     * retrieves the list of saved cities from SharedPreference
     * @return stored LinkedList<String> else returns a new empty LinkedList<String>
     */
    private LinkedList<String> loadCityPref() {
        //retrieve stored cities
        cityPref = getSharedPreferences(CITY_PREF, MODE_PRIVATE);
        cityEditor = cityPref.edit();
        String jsonCity = cityPref.getString(CITY_PREF,null);

        if (jsonCity != null) {
            emptyTextView.setVisibility(View.VISIBLE);
            return gson.fromJson(jsonCity, new TypeToken<LinkedList<String>>(){}.getType());
        }

        return new LinkedList<>();
    }

    /**
     * retrieves the list of saved weather information from SharedPreference
     * @return stored HashMap<String, WeatherResult>
     */
    private HashMap<String, WeatherResults> loadWeatherPref() {
        weatherPref = getSharedPreferences(CITY_PREF, MODE_PRIVATE);
        weatherEditor = weatherPref.edit();
        String jsonWeather = weatherPref.getString(WEATHER_PREF, null);

        if (jsonWeather != null) {
            return gson.fromJson(jsonWeather, new TypeToken<HashMap<String, WeatherResults>>(){}.getType());
        }
        return new HashMap<>();
    }

    /**
     * sets behavior for move items around and also swiping to delete
     * @return ItemTouchHelper.SimpleCallback
     */
    private ItemTouchHelper.SimpleCallback createCallBack(){
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP|ItemTouchHelper.DOWN, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

            /**
             * Swaps items in recycle view and updates sharedpreference of new positions
             * @param recyclerView: RecyclerView
             * @param viewHolder: ViewHolder
             * @param target: ViewHolder
             * @return boolean
             */
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                weatherAdapter.onItemMove(fromPos,toPos);
                weatherAdapter.notifyItemMoved(fromPos,toPos);
                Collections.swap(cityList, fromPos, toPos);
                saveList();
                return true;
            }

            /**
             * Delete item from weather list and updates the sharedpreference. Alert dialog pops up
             * to confirm deletion
             * @param viewHolder: ViewHolder
             * @param direction: int
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getAdapterPosition();
                new AlertDialog.Builder(WeatherActivity.this).setTitle("Delete City")
                        .setMessage(" Remove "+ weatherAdapter.getItemAt(position).getCity() + " ?")
                        .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                weatherCache.remove(cityList.remove(position));
                                weatherAdapter.notifyItemRemoved(position);
                                saveList();

                                if (cityList.isEmpty()) {
                                    Log.d("debug2", "setting empty visibility");
                                    emptyTextView.setVisibility(View.VISIBLE);
                                }
                            }})
                        .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                weatherAdapter.notifyItemChanged(position);
                            }})
                        .setCancelable(false).show();
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    weatherAdapter.onSelectedItem(viewHolder);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                weatherAdapter.onClearViewItem(viewHolder);
            }
        };
    }

    /**
     * Displays a dialog when there is no internet connection
     *
     * @param activity: WeatherActivity
     */
    public static void showOfflineDialog(final Activity activity) {
        if (activity.isFinishing()) {
            return;
        }
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.offline_dialog);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                activity.finish();
            }
        });

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
        dialogDisplayed = dialog;
    }

    public static void dismissDialog() {
        if (dialogDisplayed != null) {
            dialogDisplayed.dismiss();
            dialogDisplayed = null;
            Log.d("Dialog", "dialog is null");
        }
    }
}


//TODO: auto refresh when internet online
//TODO: clear all
//TODO: error message util class
//TODO: scroll to duplicated weather in recycler view
//question:refreshWeather fails when previously there is no internet. there is lag between the firing of the connectivity_change intent by the system.
//question:

