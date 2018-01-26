package com.shopback.nardweather;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;

/**
 * Create access to the all the weather results received, creates views for the items, and replaces
 * the content of some of the views with new data items when the original item is no longer available.
 */
public class WeatherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> cities;
    private HashMap<String, WeatherResults> dataSet;
    private Context context;
    private static final String WEATHER_FONT_PATH = "fonts/weathericons_regular_webfont.ttf";

    private Typeface weatherFont;

    private final int NORMAL = 0, OFFLINE = 1;

    WeatherAdapter(Context context, List<String> cities, HashMap<String, WeatherResults> dataSet) {
        this.cities = cities;
        this.dataSet = dataSet;
        this.context = context;
        weatherFont = Typeface.createFromAsset(context.getAssets(), WEATHER_FONT_PATH);
    }


    //inflate the item layout and create the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case NORMAL:
                View weatherView = inflater.inflate(R.layout.activtiy_weather_recycler_item,
                        parent, false);
                return new ViewHolderWeather(weatherView);
            case OFFLINE:
                View offlineView = inflater.inflate(R.layout.activity_weather_recycler_item_offline,
                        parent, false);
                return new ViewHolderOffline(offlineView);
        }

        return null;
    }

    //populates data into the item through holder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder ==  null) {
            return;
        }
        switch (holder.getItemViewType()) {
            case NORMAL:
                ViewHolderWeather vhw = (ViewHolderWeather) holder;
                configureViewHolderWeather(vhw, position);
                break;
            case OFFLINE:
                ViewHolderOffline vho = (ViewHolderOffline) holder;
                configureViewHolderOffline(vho, position);
                break;
        }
    }

    private void configureViewHolderWeather(ViewHolderWeather holder, int position) {
        //retrieve data model based on position
        WeatherResults results = dataSet.get(cities.get(position));

        //Set item views based on views and data model
        holder.getCityField().setText(results.getCity());
        holder.getLastUpdatedField().setText("Last Updated: " + results.getLastUpdated());
        holder.getTemperatureField().setText(results.getTemperature());
        holder.setTypeface(weatherFont);

        //get the id of the respective weather icon based on the weather code
        int weatherIconIdentifier = context.getResources().getIdentifier(results.getWeatherIcon(),
                "string", context.getPackageName());

        if (weatherIconIdentifier == 0) {
            holder.getWeatherIcon().setText("");
        } else {
            holder.getWeatherIcon().setText(weatherIconIdentifier);
        }
    }

    private void configureViewHolderOffline(ViewHolderOffline holder, int position) {
        //retrieve data model based on position
        WeatherResults results = dataSet.get(cities.get(position));

        //Set item views based on views and data model
        holder.getCityField().setText(results.getCity());
        holder.getLastUpdatedField().setText(R.string.last_updated_offline);
    }

    //determine the number of items
    @Override
    public int getItemCount() {
        return cities.size();
    }

    void onItemMove(int fromPosition, int toPosition) {
        String temp = cities.remove(fromPosition);
        //moving down
        if (toPosition > fromPosition) {
            cities.add(toPosition - 1, temp);
        } else {    //moving up
            cities.add(fromPosition, temp);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(cities.get(position)).getResultType().ordinal();
    }

    WeatherResults getItemAt(int position) {
        return dataSet.get(cities.get(position));
    }

    void onSelectedItem(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
    }

    void onClearViewItem(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.WHITE);
        switch (viewHolder.getItemViewType()) {
            case NORMAL:
                viewHolder.itemView.setBackgroundColor(Color.WHITE);
                break;
            case OFFLINE:
                viewHolder.itemView.setBackgroundColor(Color.parseColor("#DCDCDC"));
                break;
        }
    }
}
