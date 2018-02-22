package com.shopback.nardweather.data.local;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import com.shopback.nardweather.data.Weather;

@Database(entities = {Weather.class}, version = 2)
public abstract class WeatherDatabase extends RoomDatabase {

    private static WeatherDatabase databaseInstance;

    public abstract WeatherDao weatherDao();

    public static WeatherDatabase getInstance(Context context) {
        if (databaseInstance == null) {
            synchronized (WeatherDatabase.class) {
                if (databaseInstance == null) {
                    databaseInstance = Room.databaseBuilder(context.getApplicationContext(),
                            WeatherDatabase.class, "Weather.db").addMigrations(MIGRATION_1_2)
                            .build();
                }
                return databaseInstance;
            }
        }
        return databaseInstance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE weather CHANGE id city VARCHAR(22) NOT NULL");
        }
    };
}
