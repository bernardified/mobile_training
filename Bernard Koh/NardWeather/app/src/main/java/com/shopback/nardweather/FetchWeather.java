package com.shopback.nardweather;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bernardkoh on 5/1/18.
 */

public class FetchWeather {
    private static String OPEN_WEATHER_MAP_API_CALL =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";


    //retrieve weather info from openweathermap
    public static JSONObject getJSON(Context context, String city) {
        HttpURLConnection connection;
        try{
            String fullString = String.format(OPEN_WEATHER_MAP_API_CALL, city,
                    context.getString(R.string.open_weather_map_api_key));
            URL url = new URL(fullString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Charset", "utf-8,*");

            Log.d("Get-Request", url.toString());

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder responseString = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                responseString.append(line).append('\n');
            }
            reader.close();

            JSONObject data = new JSONObject(responseString.toString());

            Log.d("Get-Response", responseString.toString());

            if(data.getInt("cod") != 200){
                return null;
            }

            return data;

        } catch(Exception e){
            return null;
        }
    }

    //parse results
    public static WeatherResults parseResult(JSONObject data) {
        WeatherResults results = new WeatherResults();
        try {

            JSONObject details = data.getJSONArray("weather").getJSONObject(0);
            JSONObject main = data.getJSONObject("main");

            results.setCity(data.getString("name").toUpperCase(Locale.US) + ", " +
                    data.getJSONObject("sys").getString("country"));

            DateFormat df = DateFormat.getDateInstance();
            results.setLastUpdated("Last Updated: " +
                    df.format(new Date(data.getLong("dt") * 1000)));

            results.setSunrise(data.getJSONObject("sys").getLong("sunrise") * 1000);
            results.setSunset(data.getJSONObject("sys").getLong("sunset") * 1000);
            results.setTemperature(String.format("%.2f",
                    main.getDouble("temp")) + " ℃");
            results.setDetails(details.getString("description").toUpperCase(Locale.US) +
                    "\n" + "Humidity:" + main.getString("humidity") + " %" +
                    "\n" + "Pressure:" + main.getString("pressure") + " hPa");

            results.setWeatherIcon(data.getInt("id"));

        } catch (Exception e) {
            Log.e("FetchWeather", e.getMessage());
        }
        return results;
    }

}
