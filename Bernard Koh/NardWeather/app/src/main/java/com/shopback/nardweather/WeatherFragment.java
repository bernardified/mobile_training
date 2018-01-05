package com.shopback.nardweather;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeatherFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WeatherFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String DEFAULT_CITY = "Singapore,SG";

    Typeface weatherFont;

    TextView cityField, updatedField, weatherIcon, detailsField, currentTemperatureField;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public WeatherFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment WeatherFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WeatherFragment newInstance(String param1, String param2) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weathericons-regular-webfont.ttf");
        updateWeatherData(DEFAULT_CITY);

        Log.d("onCreateFragment", "done");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = rootView.findViewById(R.id.city_field);
        updatedField = rootView.findViewById(R.id.updated_field);
        weatherIcon = rootView.findViewById(R.id.weather_icon);
        detailsField = rootView.findViewById(R.id.details_field);
        currentTemperatureField = rootView.findViewById(R.id.current_temperature_field);

        weatherIcon.setTypeface(weatherFont);

        Log.d("onCreateViewFragment", "done");

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void updateWeatherData(String city) {
        Log.d("updateWeather", "executing");
        FetchWeather getWeather = new FetchWeather(getActivity(),city);
        getWeather.execute(getWeather.getParams());

    }

    public void changeCity(String city) {
        updateWeatherData(city);
    }

    class FetchWeather extends AsyncTask<FetchWeatherParams, Void, WeatherResults> {

        private FetchWeatherParams params;

        private static final String OPEN_WEATHER_MAP_API_CALL =
                "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";
        private static final String NIGHT = "n_";
        private static final String DAY = "d_";

        //Constructor
        FetchWeather(Context context, String city) {
            params = new FetchWeatherParams(context,city);
        }

         FetchWeatherParams getParams() {
            return params;
        }

        private JSONObject getJSON(Context context, String city) {
            HttpURLConnection connection;
            try{
                URL url = new URL(String.format(OPEN_WEATHER_MAP_API_CALL, city,
                        context.getString(R.string.open_weather_map_api_key)));
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

        @Override
        protected WeatherResults doInBackground(FetchWeatherParams... params) {
            JSONObject data =  getJSON(params[0].context, params[0].city);

            //parse data
            WeatherResults results = new WeatherResults();
            try {

                JSONObject details = data.getJSONArray("weather").getJSONObject(0);
                JSONObject main = data.getJSONObject("main");

                results.setCity(data.getString("name").toUpperCase(Locale.US) + ", " +
                        data.getJSONObject("sys").getString("country"));

                DateFormat df = DateFormat.getDateInstance();
                results.setLastUpdated("Last Updated: " +
                        df.format(new Date(data.getLong("dt")*1000)));

                results.setSunrise(data.getJSONObject("sys").getLong("sunrise")*1000);
                results.setSunset(data.getJSONObject("sys").getLong("sunset")*1000);
                results.setTemperature(String.format("%.2f",
                        main.getDouble("temp")) + " ℃");
                results.setDetails(details.getString("description").toUpperCase(Locale.US) +
                        "\n" + "Humidity:" + main.getString("humidity")+ " %" +
                        "\n" + "Pressure:" + main.getString("pressure")+ " hPa");

                results.setWeatherIcon(data.getInt("id"));

            } catch (Exception e) {
                Log.e("FetchWeather", e.getMessage());
            }
            return results;
        }

        @Override
        protected void onPostExecute(WeatherResults results) {
            cityField.setText(results.getCity());
            updatedField.setText(results.getLastUpdated());
            detailsField.setText(results.getDetails());
            currentTemperatureField.setText(results.getTemperature());
        }
    }

}

class WeatherResults {
    private String city, lastUpdated, details, temperature;
    private long sunrise, sunset;
    private int weatherIcon;

    void setCity(String city){
        this.city = city;
    }

    void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    void setDetails(String details) {
        this.details = details;
    }

    void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    void setWeatherIcon(int weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    void setSunset(long sunset) {
        this.sunset = sunset;
    }

    String getCity() {
        return city;
    }

    String getLastUpdated() {
        return lastUpdated;
    }

    String getDetails() { return details; }

    String getTemperature() {
        return temperature;
    }

    int getWeatherIcon() {
        return weatherIcon;
    }

    long getSunrise() {
        return sunrise;
    }

    long getSunset() {
        return sunset;
    }
}

class FetchWeatherParams {
    Context context;
    String city;

    FetchWeatherParams(Context context, String city) {
        this.context = context;
        this.city = city;
    }
}
