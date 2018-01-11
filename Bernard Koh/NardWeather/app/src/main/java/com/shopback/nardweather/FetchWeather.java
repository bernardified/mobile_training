package com.shopback.nardweather;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

class FetchWeather {

    private static final String EMPTY_STRING = "";

    /**
     * Retrieves city's weather information from openweathermap.org. The JSONObject is null if the
     * input city is empty. Invalid city name will throw an exception
     *
     * @param context: Context
     * @param city:    String
     * @return WeatherResults
     */
    static WeatherResults getWeather(Context context, String city){
        HttpURLConnection connection;
        Message errorMessage;
        Bundle b;

        String OPEN_WEATHER_MAP_API_CALL =
                "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";
        try {
            /*when there is no network connection or empty string where user did not enter any input
            * have to recheck network status as fetch weather is called before receiver is done
            * */
            if (NetworkUtil.getActiveNetworkInfo(context) == null || city.equals("")) {
                return null;
            }

            String fullString = String.format(OPEN_WEATHER_MAP_API_CALL, city,
                    context.getString(R.string.open_weather_map_api_key));

            URL url = new URL(fullString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            Log.d("Get-Request", url.toString());

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder responseString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {

                responseString.append(line).append('\n');
            }
            reader.close();
            connection.disconnect();

            JSONObject data = new JSONObject(responseString.toString());
            if (data.getInt("cod") != 200) {
                return null;
            }
            Log.d("Get-Response", responseString.toString());

            return parseResult(data);

        }catch (SocketTimeoutException e) {
            errorMessage = new Message();
            b = new Bundle();
            Log.d("Get-Response", "Connection has timed-out.");
            b.putString("errorMessage", "Connection Timeout. Retry again later.");
            errorMessage.setData(b);
            WeatherManager.getInstance().getMainThreadHandler().sendMessage(errorMessage);
        } catch (Exception e) {
            errorMessage = new Message();
            b = new Bundle();
            Log.d("Get-Response", "City not found");
            b.putString("errorMessage", "City not Found. Please enter a valid city.");
            errorMessage.setData(b);
            WeatherManager.getInstance().getMainThreadHandler().sendMessage(errorMessage);
        }
        return null;
    }

    /**
     * parse results from JSONObject and store in WeatherResults. WeatherResults attributes will be empty string
     * if JSONObject is null
     *
     * @param data: JSONObject
     * @return WeatherResults. Exception is called if any of the weather info cannot be retrieved
     */
    private static WeatherResults parseResult(JSONObject data) {
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
            temperature = String.valueOf(((Double) main.getDouble("temp")).intValue()) + "℃";

            weatherIcon = convertWeatherIcon(detailsInfo.getInt("id"),
                    data.getJSONObject("sys").getLong("sunrise") * 1000,
                    data.getJSONObject("sys").getLong("sunset") * 1000);

            /*create the WeatherResults object from the retrieved information and increment the
            number of WeatherResults object created by 1
             */
            results = new WeatherResults(city, lastUpdated, details, temperature, weatherIcon);


        } catch (Exception e) {
            results = null;
            Log.e("FetchWeather", e.getMessage());
        }
        return results;
    }

    /**
     * Adds string.xml prefix to the weather Id based on the current time
     * retreived from openweathermap.org
     *
     * @param weatherId: Integer
     * @param sunrise:   long
     * @param sunset:    long
     * @return String
     */
    private static String convertWeatherIcon(Integer weatherId, long sunrise, long sunset) {
        final String DAY_PREFIX = "wi_owm_day_";
        final String NIGHT_PREFIX = "wi_owm_night_";

        Log.d("convertWeatherIcon", weatherId.toString());

        String weatherIcon;
        long currentTime = new Date().getTime();
        if (currentTime >= sunrise && currentTime < sunset) {
            weatherIcon = DAY_PREFIX + weatherId.toString();
        } else {
            weatherIcon = NIGHT_PREFIX + weatherId.toString();
        }
        return weatherIcon;
    }

}
