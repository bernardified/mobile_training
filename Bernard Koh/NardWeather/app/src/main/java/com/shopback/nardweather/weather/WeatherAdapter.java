package com.shopback.nardweather.weather;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shopback.nardweather.R;
import com.shopback.nardweather.data.Weather;

import java.util.Collections;
import java.util.List;

/**
 * Create access to the all the weather results received, creates views for the items, and replaces
 * the content of some of the views with new data items when the original item is no longer available.
 */
public class WeatherAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Weather> weather;
    private Context context;
    private static final String WEATHER_FONT_PATH = "fonts/weathericons_regular_webfont.ttf";

    private Typeface weatherFont;

    private final int NORMAL = 0, OFFLINE = 1;

    WeatherAdapter(Context context, List<Weather> weather) {
        this.weather = weather;
        this.context = context;
        weatherFont = Typeface.createFromAsset(context.getAssets(), WEATHER_FONT_PATH);
    }

    void replaceData(List<Weather> weather) {
        this.weather.clear();
        this.weather.addAll(weather);
        notifyDataSetChanged();
    }

    //inflate the item layout and create the holder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
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

        if (holder == null) {
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
        Weather results = weather.get(position);

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
        Weather results = weather.get(position);

        //Set item views based on views and data model
        holder.getCityField().setText(results.getCity());
        holder.getLastUpdatedField().setText(R.string.last_updated_offline);
    }

    //determine the number of items
    @Override
    public int getItemCount() {
        return weather.size();
    }

    protected void onItemMove(int fromPosition, int toPosition) {
        Weather temp = weather.remove(fromPosition);
        //moving down
        if (toPosition > fromPosition) {
            weather.add(toPosition - 1, temp);
        } else {    //moving up
            weather.add(fromPosition, temp);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return weather.get(position).getResultType();
    }

    protected Weather getItemAt(int position) {
        return weather.get(position);
    }

    protected void onSelectedItem(RecyclerView.ViewHolder viewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
    }

    protected void onClearViewItem(RecyclerView.ViewHolder viewHolder) {
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

    static class ViewHolderOffline extends RecyclerView.ViewHolder {
        //view holder of offline items
        private TextView cityField, lastUpdatedField;

        /*
            Constructor of the entire item row and carry out view lookups to find each subview
             */
        ViewHolderOffline(View itemView) {
            super(itemView);
            cityField = itemView.findViewById(R.id.city_field);
            lastUpdatedField = itemView.findViewById(R.id.updated_field);
        }

        TextView getCityField() {
            return cityField;
        }

        TextView getLastUpdatedField() {
            return lastUpdatedField;
        }

    }

    static class ViewHolderWeather extends RecyclerView.ViewHolder {

        //normal weather view holder
        private TextView cityField, lastUpdatedField, weatherIcon, temperatureField;

        /*
        Constructor of the entire item row and carry out view lookups to find each subview
         */
        ViewHolderWeather(View itemView) {
            super(itemView);
            cityField = itemView.findViewById(R.id.city_field);
            lastUpdatedField = itemView.findViewById(R.id.updated_field);
            weatherIcon = itemView.findViewById(R.id.weather_icon);
            temperatureField = itemView.findViewById(R.id.current_temperature_field);

        }

        TextView getCityField() {
            return cityField;
        }

        TextView getLastUpdatedField() {
            return lastUpdatedField;
        }

        TextView getWeatherIcon() {
            return weatherIcon;
        }

        TextView getTemperatureField() {
            return temperatureField;
        }

        void setTypeface(Typeface weatherFont) {
            weatherIcon.setTypeface(weatherFont);
        }
    }

    /**
     * sets behavior for move items around and also swiping to delete
     *
     * @return ItemTouchHelper.SimpleCallback
     */
    ItemTouchHelper.SimpleCallback createCallBack(final WeatherContract.Presenter weatherPresenter) {
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

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
                onItemMove(fromPos, toPos);
                notifyItemMoved(fromPos, toPos);
                Collections.swap(weather, fromPos, toPos);
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
                weatherPresenter.deleteCity(weather.get(position).getId());
                weather.remove(position);
                notifyItemRemoved(position);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    onSelectedItem(viewHolder);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                onClearViewItem(viewHolder);
            }
        };
    }
}
