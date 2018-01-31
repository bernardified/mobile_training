package com.shopback.nardweather.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.shopback.nardweather.data.Weather;

@Database(entities = {Weather.class}, version = 1)
public abstract class WeatherDatabase extends RoomDatabase {

    private static WeatherDatabase databaseInstance;

    public abstract WeatherDao weatherDao();

    public static WeatherDatabase getInstance(Context context) {
        if (databaseInstance == null) {
            synchronized (WeatherDatabase.class) {
                if (databaseInstance == null) {
                    databaseInstance = Room.databaseBuilder(context.getApplicationContext(),
                            WeatherDatabase.class, "Weather.db").build();
                }
                return databaseInstance;
            }
        }
        return databaseInstance;
    }
}
