package com.shopback.nardweather;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;

/**
 * Create access to the all the weather results received, creates views for the items, and replaces
 * the content of some of the views with new data items when the original item is no longer available.
 */
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder>  {
    private List<WeatherResults> dataSet;
    private Context context;
    private static final String WEATHER_FONT_PATH = "fonts/weathericons_regular_webfont.ttf";

    /*
    Provide direct reference to each of the views within a data item
    Used to cache the views within the item layout for fast access
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView cityField, lastUpdatedField, weatherIcon, temperatureField;
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), WEATHER_FONT_PATH);

        /*
        Constructor of the entire item row and carry out view lookups to find each subview
         */
        ViewHolder(View itemView) {
            super(itemView);
            cityField = itemView.findViewById(R.id.city_field);
            lastUpdatedField = itemView.findViewById(R.id.updated_field);
            weatherIcon = itemView.findViewById(R.id.weather_icon);
            weatherIcon.setTypeface(weatherFont);
            temperatureField =  itemView.findViewById(R.id.current_temperature_field);
        }

    }

    WeatherAdapter(Context context, List<WeatherResults> dataSet) {
        this.dataSet = dataSet;
        this.context = context;
    }


    //inflate the item layout and create the holder
    @Override
    public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //Inflate custom layout
        View weatherView = inflater.inflate(R.layout.activtiy_weather_recycler_item,
                parent,false);

        //return a new holder instance
        return new ViewHolder(weatherView);
    }

    //populates data into the item through holder
    @Override
    public void onBindViewHolder(WeatherAdapter.ViewHolder holder, int position) {
        //retrieve data model based on position
        WeatherResults results = dataSet.get(position);

        //Set item views based on views and data model
        Log.d("updateUI", "updating UI");
        holder.cityField.setText(results.getCity());
        holder.lastUpdatedField.setText(results.getLastUpdated());
        holder.temperatureField.setText(results.getTemperature());


        //get the id of the respective weather icon based on the weather code
        int weatherIconIdentifier = context.getResources().getIdentifier(results.getWeatherIcon(),
                "string", context.getPackageName());

        if (weatherIconIdentifier == 0) {
            holder.weatherIcon.setText("");
        } else {
            holder.weatherIcon.setText(weatherIconIdentifier);
        }
    }
    //determine the number of items
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    void onItemMove(int fromPosition, int toPosition) {
        WeatherResults temp = dataSet.remove(fromPosition);
        //moving down
        if (toPosition > fromPosition) {
            dataSet.add(toPosition-1, temp);
        } else {    //moving up
            dataSet.add(fromPosition, temp);
        }
    }

    WeatherResults getItemAt(int position) {
        return dataSet.get(position);
    }

    void onSelectedItem(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
    }

    void onClearViewItem(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.WHITE);
    }

}
