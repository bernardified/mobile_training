package com.shopback.nardweather.data.local;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.shopback.nardweather.data.Weather;

import java.util.List;

@Dao
public interface WeatherDao {

    /**
     * Get weather info of all cities from the weather table
     *
     * @return weather info of all cities
     */
    @Query("SELECT * FROM weather")
    public List<Weather> getAllWeather();

    /**
     * Get weather info of a specific city by id
     *
     * @return weather info of if
     */
    @Query("SELECT * FROM weather WHERE id = :entryId")
    Weather getWeatherById(String entryId);

    /**
     * Insert weather info into database. Replace if necessary
     *
     * @param weather the weather info to be inserted
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeather(Weather weather);

    @Update
    int updateWeather(Weather weather);

    @Query("DELETE FROM weather WHERE id = :entryId")
    int deleteWeatherById(String entryId);


}
