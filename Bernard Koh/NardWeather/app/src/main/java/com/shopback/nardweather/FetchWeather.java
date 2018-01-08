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

class FetchWeather {
    //retrieve weather info from openweathermap
    static JSONObject getJSON(Context context, String city) {
        HttpURLConnection connection;
        String OPEN_WEATHER_MAP_API_CALL =
                "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";
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
    static WeatherResults parseResult(JSONObject data) {
        WeatherResults results;
        try {
            JSONObject detailsInfo, main;
            String city, lastUpdated, details, temperature, weatherIcon;
            DateFormat df;

            detailsInfo = data.getJSONArray("weather").getJSONObject(0);
            main = data.getJSONObject("main");

            city = data.getString("name").toUpperCase(Locale.US) + ", " +
                    data.getJSONObject("sys").getString("country");

            df = DateFormat.getDateTimeInstance();
            lastUpdated = "Last Updated: " +
                    df.format(new Date(data.getLong("dt") * 1000)) + " GMT";
            details = detailsInfo.getString("description").toUpperCase(Locale.US) +
                    "\n" + "Humidity:" + main.getString("humidity") + " %" +
                    "\n" + "Pressure:" + main.getString("pressure") + " hPa";
            temperature = String.valueOf(((Double)main.getDouble("temp")).intValue()) + "℃";

            weatherIcon = convertWeatherIcon(detailsInfo.getInt("id"),
                    data.getJSONObject("sys").getLong("sunrise") * 1000,
                    data.getJSONObject("sys").getLong("sunset") * 1000);

            results = new WeatherResults(city,lastUpdated,details,temperature, weatherIcon);

        } catch (Exception e) {
            results = null;
            Log.e("FetchWeather", e.getMessage());
        }
        return results;
    }

    //convert input weather icon id to string.xml format
    private static String convertWeatherIcon (Integer weatherId, long sunrise, long sunset) {
        final String DAY_PREFIX = "wi_owm_day_";
        final String NIGHT_PREFIX = "wi_owm_night_";

        Log.d("convertWeatherIcon", weatherId.toString());

        String weatherIcon;
        long currentTime = new Date().getTime();
        if(currentTime >= sunrise && currentTime < sunset) {
            weatherIcon = DAY_PREFIX + weatherId.toString();
        } else {
            weatherIcon = NIGHT_PREFIX + weatherId.toString();
        }
        return weatherIcon;
    }

}
