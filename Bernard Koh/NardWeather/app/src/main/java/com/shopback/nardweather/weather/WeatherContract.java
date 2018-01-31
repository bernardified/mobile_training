package com.shopback.nardweather.weather;


import android.content.Context;

import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.util.BasePresenter;
import com.shopback.nardweather.util.BaseView;

import java.util.List;

/**
 * Specification of contract between the weather view and the weather presenter
 */
public interface WeatherContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showWeather(List<Weather> weatherList);

        void showMessage(String Message);

    }

    interface Presenter extends BasePresenter {

        void loadWeather(boolean forceUpdate);

        void addNewCities(Context context, List<String> newCities);

        void deleteCity(String city);
    }
}
