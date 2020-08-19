package com.example.coolweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.JSONUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.Util;


public class myFragment extends Fragment {
    ListView position;

    List<Province> provinces = new ArrayList<>();

    List<City> citys = new ArrayList<>();

    List<County> counties = new ArrayList<>();

    List<String> dataList = new ArrayList<>();

    ArrayAdapter<String> adapter = null;

    final int LEVEL_PROVINCE = 0;

    final int LEVEL_CITY = 1;

    final int LEVEL_COUNTY = 2;

    int current_level = -1;

    Province selected_province;

    City selected_city;

    County selected_county;

    String weather;


    String url = "http://guolin.tech/api/china";





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_main,container,false);


        position = view.findViewById(R.id.positon);

        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);

        position.setAdapter(adapter);

        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        queryProvince();

        position.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(current_level == LEVEL_PROVINCE){

                    selected_province = provinces.get(i);

                    Toast.makeText(getContext(),selected_province.getProvinceName(),Toast.LENGTH_SHORT).show();

                    queryCity();
                }else if (current_level == LEVEL_CITY){

                    selected_city = citys.get(i);

                    Toast.makeText(getContext(),selected_city.getCityName(),Toast.LENGTH_SHORT).show();

                    queryCounty();
                }else if (current_level == LEVEL_COUNTY){
                    selected_county = counties.get(i);

                    Toast.makeText(getContext(),selected_province.getProvinceName(),Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getActivity(), MainActivity.class);

                    intent.putExtra("weather_id",String.valueOf(selected_county.getWeatherId()));

                    startActivity(intent);
                }
            }
        });

        super.onActivityCreated(savedInstanceState);


    }


    public void queryProvince(){

        provinces = LitePal.findAll(Province.class);

        if (provinces.size()>0){

            dataList.clear();
            for (Province province: provinces){
                String province_name = province.getProvinceName();
                dataList.add(province_name);
            }
            adapter.notifyDataSetChanged();
            position.setSelection(0);
            current_level = LEVEL_PROVINCE;
        }else {
            queryFromInternet("http://guolin.tech/api/china","province");
        }
    }

    public void queryCity(){
        citys = LitePal.where("provinceId = ?",String.valueOf(selected_province.getProvinceCode())).find(City.class);
        if (citys.size()>0){
            Log.d("cool111","启动从本地数据库查找城市");
            dataList.clear();
            for (City city:citys){
                String city_name = city.getCityName();
                dataList.add(city_name);
            }
            adapter.notifyDataSetChanged();
            position.setSelection(0);
            current_level = LEVEL_CITY;

        }else {
            String url_city = "http://guolin.tech/api/china/" + selected_province.getProvinceCode();
            queryFromInternet(url_city,"city");
        }
    }

    public void queryCounty(){
        counties = LitePal.where("cityId = ?",String.valueOf(selected_city.getCityCode())).find(County.class);
        if(counties.size()>0){
            dataList.clear();
            for (County county:counties){
                String county_name = county.getCountyName();
                dataList.add(county_name);
            }
            adapter.notifyDataSetChanged();
            position.setSelection(0);
            current_level = LEVEL_COUNTY;

        }else {
            String url_county = "http://guolin.tech/api/china/"  + selected_province.getProvinceCode()+"/" + selected_city.getCityCode();
            queryFromInternet(url_county,"county");
        }
    }

    public void queryWeatherInfo(){
        SharedPreferences spf = getActivity().getSharedPreferences("weather",Context.MODE_PRIVATE);

        weather = spf.getString("weather","");

        if (weather == null){

        }
    }


    public void queryFromInternet(String url,final String type){
        HttpUtil.sendOkhttpRequst(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();

                boolean result = false;

                if ("province".equals(type)){
                    result = JSONUtil.handleProvinceWithJSON(responseText);
                }else if ("city".equals(type)){
                    result = JSONUtil.handleCityWithJSON(responseText,selected_province.getProvinceCode());
                }else if ("county".equals(type)){
                    result = JSONUtil.handleCountyWithJSON(responseText,selected_city.getCityCode());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("province".equals(type)){
                                queryProvince();
                            }else if ("city".equals(type)){
                                queryCity();
                            }else if ("county".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    public void queryWeatherFromService(String url){
        HttpUtil.sendOkhttpRequst(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String weather_info = response.body().string();

                Weather my_weather = JSONUtil.handleWeatherWithGson(weather_info);


            }
        });
    }

}
