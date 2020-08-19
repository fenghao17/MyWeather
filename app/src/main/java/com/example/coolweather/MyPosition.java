package com.example.coolweather;


import android.content.Context;
import android.location.LocationListener;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;



public class MyPosition {

    public static LocationClient mLocationClient;

    public static void return_position(Context context){

        mLocationClient = new LocationClient(context);

        mLocationClient.registerLocationListener(new TLocationListener());

        mLocationClient.start();

    }

    public void initLocation(){
        LocationClientOption option = new LocationClientOption();

        option.setScanSpan(5000);

        option.setIsNeedAddress(true);

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        mLocationClient.setLocOption(option);
    }

  static class TLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d("111www", bdLocation.getLatitude()+"/"+ bdLocation.getLongitude());

            Log.d("111www", bdLocation.getCountry()+"/"+ bdLocation.getCity());
        }
    }



}

