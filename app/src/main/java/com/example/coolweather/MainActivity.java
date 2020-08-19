package com.example.coolweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.AQI;
import com.example.coolweather.gson.Basic;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Now;
import com.example.coolweather.gson.Suggestion;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.JSONUtil;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //头部定义
    ImageView home_as_up;

    TextView time;

    DrawerLayout drawerLayout;

    //定义天气布局
    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    //相关数据定义

    List<Forecast> forecasts = new ArrayList<>();

    //定位
    LocationClient mLocationClient;

    String province_name;

    String city_name;

    String county_name;

    int province_code;

    int province_id;

    int city_code;

    int city_id;

    String weather_id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取定位接口
        SDKInitializer.initialize(getApplicationContext());
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());

        MyPosition.return_position(this);

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer);

        //创建数据库
        Connector.getWritableDatabase();

        //heard bar
        home_as_up = findViewById(R.id.home_as_up);

        time = findViewById(R.id.time);

        //获取天气布局

        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.comfort_text);
        sportText = findViewById(R.id.sport_text);


        //获取天气信息
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = spf.getString("weather_text",null);


        //申请权限
        requestPermission();

        Log.d("findbug","定位位置:" + province_name + city_name + county_name);

        if (getIntent()==null){
            Log.d("findbug","执行无活动启用");


            if (weatherString!=null){

                Log.d("findbug","执行无本地数据库");

                Weather weather = JSONUtil.handleWeatherWithGson(weatherString);

                showWeatherInfo(weather);
            }else {
                //定位初始天气信息
                Log.d("findbug","执行有活动启用");
                List<Province> provinceList = LitePal.findAll(Province.class);

                if (!provinceList.isEmpty()){
                    for (Province province:provinceList){
                        if (province.getProvinceName()==province_name){
                            province_code = province.getProvinceCode();
                        }
                    }
                }

                Log.d("findbug","省查找完毕");

                Log.d("findbug","省ID"+province_code);

                String url_city = "http://guolin.tech/api/china/" + province_code;

                HttpUtil.sendOkhttpRequst(url_city, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String city_response = response.body().string();
                        JSONUtil.handleCityWithJSON(city_response,province_id);

                        List<City> cityList = LitePal.findAll(City.class);

                        if (!cityList.isEmpty()){
                            for (City city:cityList){
                                if (city.getCityName() == city_name){
                                    city_code = city.getCityCode();
                                }
                            }
                        }
                    }
                });

                Log.d("findbug","市查找完毕");

                String url_county = "http://guolin.tech/api/china/" + province_code + "/" + city_code;

                HttpUtil.sendOkhttpRequst(url_county, new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String county_response = response.body().string();
                        JSONUtil.handleCountyWithJSON(county_response,city_id);

                        List<County> countyList = LitePal.findAll(County.class);

                        if (!countyList.isEmpty()){
                            for (County county:countyList){
                                if (county.getCountyName() == county_name){
                                    weather_id = county.getWeatherId();
                                }
                            }
                        }
                    }
                });

                requestWeather(weather_id);

            }
        }else {
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }




    }




    //获取定位城市
    public void getCurrentPosition(){
        initLocation();
        mLocationClient.start();

    }

    public class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location){


            province_name = location.getProvince();

            Log.d("findbug","定位位置:"+province_name+city_name+county_name);

            city_name = location.getCity();

            county_name = location.getDistrict();

        }

    }

    public void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
    }

    //向服务器请求天气信息
    public void requestWeather(final String weatherId){


        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=2b1f5b4912994e55a27ff785a99edcf3";

        HttpUtil.sendOkhttpRequst(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            final String response_text = response.body().string();
                try {

                    final Weather weather = JSONUtil.handleWeatherWithGson(response_text);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (weather != null && "ok".equals(weather.status)){
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();

                                editor.putString("weather_text",response_text);

                                editor.apply();

                                showWeatherInfo(weather);
                            }else {
                                Toast.makeText(MainActivity.this,"获取天气信心失败",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }
        });
    }

    //展示天气信息
    public void showWeatherInfo(Weather weather){

        AQI aqi = weather.aqi;

        Basic basic = weather.basic;

        forecasts = weather.forecastList;

        Now now = weather.now;

        Suggestion suggestion = weather.suggestion;

        String cityName = basic.cityName;

        String updateTime = basic.update.updateTime.split(" ")[1];

        String degree = now.temperature + "摄氏度";

        String weatherInfo = now.more.info;

        titleCity.setText(cityName);


        degreeText.setText(degree);


        weatherInfoText.setText(weatherInfo);


        forecastLayout.removeAllViews();

        for (Forecast forecast: forecasts){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText = view.findViewById(R.id.date_text);

            TextView infoText = view.findViewById(R.id.info_text);

            TextView maxText = view.findViewById(R.id.max_text);

            TextView minText = view.findViewById(R.id.min_text);

            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }if (aqi!=null){
            aqiText.setText(aqi.city.aqi);
            pm25Text.setText(aqi.city.pm25);
        }

        String comfort = "舒适度：" + suggestion.comfort.info;

        String carWash = "洗车指数" + suggestion.carWash.info;

        String sport = "运动指数" + suggestion.sport.info;

        comfortText.setText(comfort);

        carWashText.setText(carWash);

        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);

    }


    //获取权限

    public void requestPermission(){
        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,1);
        }else {
           // getCurrentPosition();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for(int result : grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                }
                //getCurrentPosition();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
           case  R.id.home_as_up:
               drawerLayout.openDrawer(GravityCompat.START);
            break;
        }
    }
}
