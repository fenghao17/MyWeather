package com.example.coolweather.util;

import android.util.Log;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONObject;


public class JSONUtil {

    public static boolean handleProvinceWithJSON(String response){
        try {

            JSONArray jsonArray = new JSONArray(response);

            for (int i=0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                Province province = new Province();

                province.setProvinceCode(jsonObject.getInt("id"));
                province.setProvinceName(jsonObject.getString("name"));

                province.save();

            }
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean handleCityWithJSON(String response, final int provinceId){

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i=0;i < jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                City city = new City();
                                city.setCityName(jsonObject.getString("name"));
                                city.setCityCode(jsonObject.getInt("id"));
                                city.setProvinceId(provinceId);
                                city.save();
                            }
                            return true;
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }

                    return false;
    }

    public static boolean handleCountyWithJSON(String response, final int cityId){

                try{

                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        County county = new County();

                        county.setCityId(cityId);

                        county.setCountyName(jsonObject.getString("name"));

                        county.setId(jsonObject.getInt("id"));

                        county.setWeatherId(jsonObject.getString("weather_id"));

                        county.save();
                    }
                    return true;

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            return false;
    }

    public static Weather handleWeatherWithGson(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);

            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");

            String weather_info = jsonArray.getJSONObject(0).toString();

            return new Gson().fromJson(weather_info, Weather.class);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

}
