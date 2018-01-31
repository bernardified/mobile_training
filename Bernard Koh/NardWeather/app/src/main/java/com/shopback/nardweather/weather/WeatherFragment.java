package com.shopback.nardweather.weather;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.shopback.nardweather.R;
import com.shopback.nardweather.data.Weather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class WeatherFragment extends Fragment implements WeatherContract.View {

    private WeatherContract.Presenter weatherPresenter;

    private WeatherAdapter weatherAdapter;

    private RecyclerView weatherRecyclerView;

    private static Dialog dialogDisplayed;

    private TextView emptyTextView;

    SwipeRefreshLayout swipeContainer;

    public WeatherFragment() {
        //constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherAdapter = new WeatherAdapter(getContext(), new ArrayList<Weather>(0));
    }

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        weatherPresenter.start();
        //set ItemTouchHelper to delete item on recycle view
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(weatherAdapter.createCallBack(weatherPresenter));
        itemTouchHelper.attachToRecyclerView(weatherRecyclerView);
    }

    @Override
    public void setPresenter(WeatherContract.Presenter presenter) {
        weatherPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_weather_recycler, container,
                false);

        emptyTextView = root.findViewById(R.id.weather_list_empty);
        weatherRecyclerView = root.findViewById(R.id.weather_recycler_view);
        weatherRecyclerView.setHasFixedSize(true);
        weatherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //creating of adapter and link to recycler view
        weatherRecyclerView.setAdapter(weatherAdapter);

        swipeContainer = root.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                weatherPresenter.loadWeather(true);
                weatherAdapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(android.R.color.holo_red_dark);

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.weather, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_cities:
                showInputDialog();
        }
        return true;
    }

    /**
     * shows the input Alert Dialog with 3 edit text field  and two buttons for user to enter city name
     */
    private void showInputDialog() {

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View inputDialog = inflater.inflate(R.layout.add_cities_input_dialog, null);

        final EditText inputOne = inputDialog.findViewById(R.id.inputOne);
        final EditText inputTwo = inputDialog.findViewById(R.id.inputTwo);
        final EditText inputThree = inputDialog.findViewById(R.id.inputThree);

        new AlertDialog.Builder(getContext()).setTitle("Search by City")
                .setMessage("Please enter cities:").setView(inputDialog)
                .setPositiveButton(R.string.dialog_go, new DialogInterface.OnClickListener() {
                    @Override
                    //change city when Go Button pressed and store city in SharedPreference
                    public void onClick(DialogInterface dialog, int id) {
                        weatherPresenter.addNewCities(getContext(),new LinkedList<>(Arrays.asList(
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
        }).create().show();
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showWeather(List<Weather> weatherList) {
        weatherAdapter.replaceData(weatherList);
    }

    @Override
    public void showMessage(String Message) {

    }
}
