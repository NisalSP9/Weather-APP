package com.android.example.cis.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ArrayAdapter<String> mforecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {

            FatchWeatherTask fatchWeatherTask = new FatchWeatherTask();
            fatchWeatherTask.execute("1231410");
            return true;
        }

        return super.onOptionsItemSelected(item);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forcastArray = {};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forcastArray));

        mforecastAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_forcast, weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setAdapter(mforecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String forecast = mforecastAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), Detail_Activity.class);

                intent.putExtra(intent.EXTRA_TEXT, forecast);

                startActivity(intent);


            }
        });
        return rootView;

    }

    public class FatchWeatherTask extends AsyncTask<String, Void, String[]> {


        String[] infor;


        @Override
        protected String[] doInBackground(String... params) {


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJSONString;

            String format = "JSON";
            String units = "metric";
            int numberOfDays = 7;
            String key = "4573800ba8c2da2be0bb5967fcf14ba0";
            //&appid=4573800ba8c2da2be0bb5967fcf14ba0


            try {


                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";

                final String QUERY_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAMS = "cnt";
                final String APPID_PARAM = "appid";

                Uri uriBuilder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAMS, numberOfDays + "")
                        .appendQueryParameter(APPID_PARAM, key).build();


                // URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?id=1231410&mode=json&cnt=7&units=metric&appid=4573800ba8c2da2be0bb5967fcf14ba0");

                URL url = new URL(uriBuilder.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();

                if (null == inputStream) {

                    return null;

                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;


                while ((line = reader.readLine()) != null) {


                    buffer.append(line + "\n");


                }

                if (buffer.length() == 0) {

                    return null;
                }

                forecastJSONString = buffer.toString();

                //Log.d(LOG_TAG, "Forecast JSON String " + forecastJSONString);

                infor = getWeatherInfor(forecastJSONString, numberOfDays);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                if (null == urlConnection) {

                    urlConnection.disconnect();

                }

                if (null != reader) {

                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }

            return infor;
        }


        private String dateFormater(long date) {


            Date date1 = new Date(date * 1000);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, MMM d");
            String dateInforString = simpleDateFormat.format(date1).toString();

            return dateInforString;

        }


        public String[] getWeatherInfor(String forecastJSONString, int NOD) throws JSONException {


            String[] forecastDeals = new String[NOD];

            //Log.d("calling", "getWeatherInfor: \n");


            JSONObject jsonObjectWeather = new JSONObject(forecastJSONString);
            JSONArray date = jsonObjectWeather.getJSONArray("list");//get the list array

            for (int j = 0; j < NOD; j++) {

                //get all information

                JSONObject dayInfor = date.getJSONObject(j);// get the date  by array index


                //get the real calender date
                long dateLongValue = dayInfor.getLong("dt");
                String dateInforString = dateFormater(dateLongValue);


                //get temp information
                JSONObject tempInfor = dayInfor.getJSONObject("temp");
                String maxTempString = tempInfor.getDouble("max") + "";
                String minTempString = tempInfor.getDouble("min") + "";

                //get weather information weather is an array
                JSONArray weatherSum = dayInfor.getJSONArray("weather");
                JSONObject mainWeatherSum = weatherSum.getJSONObject(0);//main information on this 0th index
                String mainWeatherString = mainWeatherSum.getString("main");


                String output = dateInforString + "-" + mainWeatherString + "-" + maxTempString + "/" + minTempString;

                forecastDeals[j] = output;


            }

//            for (String x : forecastDeals) {
//
//                Log.d("array", x);
//
//            }

            return forecastDeals;
        }

        @Override
        protected void onPostExecute(String[] result) {
            //super.onPostExecute(strings);

            if (result != null) {

                mforecastAdapter.clear();

                for (String dayForecast : result) {

                    mforecastAdapter.add(dayForecast);

                }

            }
        }
    }
}
//    private String[] getWeatherInfor(String forecastJsonStr, int numDays)
//            throws JSONException {
//
//        // These are the names of the JSON objects that need to be extracted.
//        final String OWM_LIST = "list";
//        final String OWM_WEATHER = "weather";
//        final String OWM_TEMPERATURE = "temp";
//        final String OWM_MAX = "max";
//        final String OWM_MIN = "min";
//        final String OWM_DATETIME = "dt";
//        final String OWM_DESCRIPTION = "main";
//
//        JSONObject forecastJson = new JSONObject(forecastJsonStr);
//        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//        String[] resultStrs = new String[numDays];
//        for(int i = 0; i < weatherArray.length(); i++) {
//            // For now, using the format "Day, description, hi/low"
//            String day;
//            String description;
//            String highAndLow;
//
//            // Get the JSON object representing the day
//            JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//            // The date/time is returned as a long.  We need to convert that
//            // into something human-readable, since most people won't read "1400356800" as
//            // "this saturday".
//            long dateTime = dayForecast.getLong(OWM_DATETIME);
//            day = getReadableDateString(dateTime);
//
//            // description is in a child array called "weather", which is 1 element long.
//            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//            description = weatherObject.getString(OWM_DESCRIPTION);
//
//            // Temperatures are in a child object called "temp".  Try not to name variables
//            // "temp" when working with temperature.  It confuses everybody.
//            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//            double high = temperatureObject.getDouble(OWM_MAX);
//            double low = temperatureObject.getDouble(OWM_MIN);
//
//            highAndLow = formatHighLows(high, low);
//            resultStrs[i] = day + " - " + description + " - " + highAndLow;
//        }
//
//        return resultStrs;
//    }