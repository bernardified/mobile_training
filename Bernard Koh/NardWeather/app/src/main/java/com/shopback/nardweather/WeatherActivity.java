package com.shopback.nardweather;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;


public class WeatherActivity extends AppCompatActivity {
    public static final int INVALID_CITY = 3;
    public static final int DUPLICATE_CITY = 4;
    public static final String CITY_PREF = "City";
    public static Handler postToUiHandler;

    static Dialog dialogDisplayed;
    TextView emptyTextView;

    NetworkReceiver networkReceiver;
    IntentFilter filter = new IntentFilter();
    WeatherManager weatherManager;  //managed thread pool to schedule jobs
    LinkedList<WeatherResults> weatherList;   //list of weather results

    WeatherAdapter weatherAdapter;
    RecyclerView weatherRecyclerView;

    SharedPreferences cityPref;
    SharedPreferences.Editor prefEditor;
    Gson gson; //used to serialize and deserialize weatherList into json string object


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
        //retrieve saved weather results from SharedPreference
        weatherList = loadPref();
        //connecting to layout manager
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //creating of adapter and link to recycler view
        weatherAdapter = new WeatherAdapter(this, weatherList);
        weatherRecyclerView.setAdapter(weatherAdapter);
        //set ItemTouchHelper to delete item on recycle view
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createCallBack());
        itemTouchHelper.attachToRecyclerView(weatherRecyclerView);
    }

    /**
     * registers network receiver during onStart callback
     */
    @Override
    public void onStart() {
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
        super.onStart();
    }

    /**
     * deregisters network register during onStop callback
     */
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
        emptyTextView.setVisibility(weatherList.isEmpty() ? View.INVISIBLE : View.INVISIBLE);
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
                        updateWeather(inputOne.getText().toString(), inputTwo.getText().toString(),
                                inputThree.getText().toString());
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
        saveList();
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
                Log.d("Network", "fetching " + city);
                WeatherResults data = FetchWeather.getWeather(WeatherActivity.this, city);
                if (data != null && !isDuplicating(data.getCity())) {
                    weatherList.add(data);
                    postToUiHandler.post(getUiRunnable());
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
     * @return Runnable
     */
    private Runnable getUiRunnable() {
        return new Runnable() {
            public void run() {
                Log.d("Network", "updating UI");
                if (!weatherList.isEmpty()) {
                    emptyTextView.setVisibility(View.INVISIBLE);
                }
                weatherAdapter.notifyDataSetChanged();
                weatherRecyclerView.scrollToPosition(weatherAdapter.getItemCount()-1);
            }
        };
    }


    /**
     * retrieves the list of saved weather information from SharedPreference
     * @return stored LinkedList<WeatherResult> else returns a new empty LinkedList<WeatherResult>
     */
    private LinkedList<WeatherResults> loadPref() {
        //retrieve stored cities
        cityPref = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = cityPref.edit();
        String json = cityPref.getString(CITY_PREF,null);
        if (json != null) {
            return gson.fromJson(json, new TypeToken<LinkedList<WeatherResults>>(){}.getType());
        }
        return new LinkedList<>();
    }

    /**
     * Iterates through the weather list and checks if the input city is already in the list
     * @param city: String
     * @return true if there is a duplicate else return false
     */
    private boolean isDuplicating(String city) {
        Message message;
        Bundle b;

        ListIterator<WeatherResults> iterator = weatherList.listIterator();
        WeatherResults next;
        while (iterator.hasNext()) {
            next = iterator.next();
            if (next.getCity().equals(city)) {
                message = new Message();
                b = new Bundle();
                message.what = WeatherActivity.DUPLICATE_CITY;
                b.putString("errorMessage", next.getCity());
                message.setData(b);
                WeatherManager.getInstance().getMainThreadHandler().sendMessage(message);
                return true;
            }
        }
        return false;
    }


    /**
     * Saves the current weather list to SharedPreference
     */
    private void saveList() {
        String json = gson.toJson(weatherList);
        prefEditor.putString(CITY_PREF, json);
        prefEditor.apply();
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
                Collections.swap(weatherList, fromPos, toPos);
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
                                weatherList.remove(position);
                                weatherAdapter.notifyItemRemoved(position);
                                saveList();

                                if (weatherList.isEmpty()) {
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
     * @param context: Context
     */
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

    public static void dismissDialog() {
        if (dialogDisplayed != null) {
            dialogDisplayed.dismiss();
        }
    }

    private static void setDialog(Dialog dialog){
        dialogDisplayed = dialog;
    }

    public static Dialog getDialog() {
        return dialogDisplayed;
    }

}


//TODO: Refresh feature
//TODO: clear all
//TODO: timezone
//TODO: error message util class
//TODO: scroll to duplciated weather in recycler view
