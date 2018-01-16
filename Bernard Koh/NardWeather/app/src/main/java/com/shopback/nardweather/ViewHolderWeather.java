package com.shopback.nardweather;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class ViewHolderWeather extends RecyclerView.ViewHolder {

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
