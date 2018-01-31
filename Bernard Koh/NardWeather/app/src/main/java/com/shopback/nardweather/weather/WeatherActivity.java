package com.shopback.nardweather.weather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.shopback.nardweather.R;
import com.shopback.nardweather.data.WeatherStorage;
import com.shopback.nardweather.data.local.LocalWeatherDataSource;
import com.shopback.nardweather.data.local.WeatherDatabase;
import com.shopback.nardweather.util.ThreadManager;

public class WeatherActivity extends AppCompatActivity {

    private WeatherPresenter weatherPresenter;

    private WeatherFragment weatherView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        WeatherDatabase database = WeatherDatabase.getInstance(this);
        ThreadManager threadManager = ThreadManager.getInstance(this);
        WeatherStorage weatherStorage = WeatherStorage.getInstance(
                LocalWeatherDataSource.getInstance(threadManager, database.weatherDao()));
        weatherView = (WeatherFragment) getFragmentManager().findFragmentById(R.id.weather_fragment);

        weatherPresenter = new WeatherPresenter(weatherStorage, weatherView, threadManager);
    }
}
