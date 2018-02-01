package com.shopback.nardweather.weather;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.shopback.nardweather.NoArchitecture.NetworkReceiver;
import com.shopback.nardweather.R;
import com.shopback.nardweather.data.Weather;
import com.shopback.nardweather.util.Util;

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
    private static final String DEFAULT_SINGAPORE = "Singapore, SG";
    private static final String OPEN_WEATHER_MAP_API_CALL = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";
    private static final String OPEN_WEATHER_API_KEY = "1bab5a3cc4e7423879bea7b2dea70edc";

    /**
     * Retrieves city's weather information from openweathermap.org. The JSONObject is null if the
     * input city is empty. Invalid city name will throw an exception
     *
     * @param city:    String
     * @return WeatherResults
     */
    static Weather getWeather(String city){
        HttpURLConnection connection;
        try {
            //ignore empty input
            if(city.equals("")) {
                return null;
            }

            if (NetworkReceiver.getInstance().getNetworkStatus() == Util.NETWORK_ERROR_ID) {
                Log.d("getWeather", "no network");
                Weather result = new Weather(city, "","","","");
                result.setResultType(Weather.ResultType.OFFLINE.ordinal());
                return result;
            }

            if (city.equalsIgnoreCase("singapore") ||
                    city.equalsIgnoreCase("singapore,my")) {
                city = DEFAULT_SINGAPORE;
            }

            String fullString = String.format(OPEN_WEATHER_MAP_API_CALL, city, OPEN_WEATHER_API_KEY);

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
            Message errorMessage = Util.generateMessage(Util.ERROR_MESSAGE_KEY, Util.NETWORK_TIMEOUT,
                    Util.CONNECTION_TIMEOUT_MESSAGE);
//            WeatherManager.getInstance().getMainThreadHandler().sendMessage(errorMessage);
//            NetworkReceiver.setNetworkStatus(Util.NETWORK_SLOW);
        } catch (Exception e) {
            Message errorMessage = Util.generateMessage(Util.ERROR_MESSAGE_KEY, Util.INVALID_CITY,
                    city + Util.INVALID_CITY_MESSAGE);
//            WeatherManager.getInstance().getMainThreadHandler().sendMessage(errorMessage);
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
    private static Weather parseResult(JSONObject data) {
        Weather results;
        try {
            JSONObject detailsInfo, main;
            String city, lastUpdated, details, temperature, weatherIcon;
            DateFormat df;

            detailsInfo = data.getJSONArray("weather").getJSONObject(0);
            main = data.getJSONObject("main");

            city = data.getString("name").toUpperCase(Locale.US) + ", " +
                    data.getJSONObject("sys").getString("country");

            df = DateFormat.getDateTimeInstance();
            lastUpdated = df.format(new Date(data.getLong("dt") * 1000));
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
            results = new Weather(city, lastUpdated, details, temperature, weatherIcon);
            results.setResultType(Weather.ResultType.NORMAL.ordinal());

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
