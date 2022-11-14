package com.example.finalexamsc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private TextInputEditText cityET;
    private ImageView backIV, iconIV, searchIV;
    private Button logout;
    private FirebaseAuth mAuth;

    private RecyclerView weatherRV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;

    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;

    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); // this is so that our apps are fullscreen
        setContentView(R.layout.activity_main);

        homeRL          = findViewById(R.id.idRelaitveLayoutHome);
        loadingPB       = findViewById(R.id.idProgressBarLoading);
        cityNameTV      = findViewById(R.id.idTextViewCityName);
        temperatureTV   = findViewById(R.id.idTextViewTemperature);
        conditionTV     = findViewById(R.id.idTextViewCondition);
        cityET          = findViewById(R.id.idTextInputEditCity);
        backIV          = findViewById(R.id.idImageViewBackground);
        iconIV          = findViewById(R.id.idImageViewIcon);
        searchIV        = findViewById(R.id.idImageViewSearch);
        weatherRV       = findViewById(R.id.idRecycleViewWeather);
        logout          = findViewById(R.id.idButtonLogout);
        mAuth           = FirebaseAuth.getInstance();

        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingPB.setVisibility(View.VISIBLE);
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location != null) {

            cityName = getCityName(location.getLongitude(),location.getLatitude());
            getWeatherInfo(cityName);

        } else {

            cityName = "London";
            getWeatherInfo(cityName);
        }

        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityET.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter a city name...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please allow the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found!";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> adresses = gcd.getFromLocation(latitude, longitude, 10);

            for (Address address : adresses){
                if(address != null){
                    String city = address.getLocality();
                    if(city != null && !city.equals("")){
                        cityName = city;
                    } else {
                        Log.d("TAG", "CITY NOT FOUND!");
                        Toast.makeText(this, "The City you entered was not found!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e)
        {
                e.printStackTrace();
        }
        return cityName;
    }


    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=8896c64c7ca84df6a73151235223105&q=" + cityName + "&days=1&aqi=no&alerts=no";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.VISIBLE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature + " °C");
                    int isDay =  response.getJSONObject("current").getInt("is_day"); // the value will be 1 or 0, yes or no respectively
                    String conditionText = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(conditionText);

                    if(isDay == 1) {
                        // day time
                        Picasso.get().load("https://w0.peakpx.com/wallpaper/520/819/HD-wallpaper-clouds-sky-day-bright.jpg").into(backIV);
                    } else {
                        // night time
                        Picasso.get().load("https://i.pinimg.com/originals/f5/90/ec/f590ec2fbf447d79556205cb264f43bc.jpg").into(backIV);
                    }

                    JSONObject forecastObject = response.getJSONObject("forecast");
                    JSONObject forecastDay = forecastObject.getJSONArray("forecastday").getJSONObject(0); // using 0 since I only need the first object
                    JSONArray hourArray = forecastDay.getJSONArray("hour");

                    // This for loop is so I can get the hourly report of the weather
                    for (int i = 0 ; i < hourArray.length(); i++){
                        JSONObject hourObject = hourArray.getJSONObject(i);
                        String time = hourObject.getString("time");
                        String tempHourly = hourObject.getString("temp_c");
                        String imgHourly = hourObject.getJSONObject("condition").getString("icon");
                        String windSpeed = hourObject.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time, tempHourly, imgHourly, windSpeed));
                    }

                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter a valid city name...", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}