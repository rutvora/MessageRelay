package rut.com.messagerelay.UserData;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;

import rut.com.messagerelay.MainActivity;

public class Location implements LocationListener {

    private Context context;

    public Location(Context context) {
        this.context = context;
    }

    public void setup(@Nullable AppCompatActivity activity) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (activity != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.LOCATION_SERVICE_REQUEST);
            } else {

                if (locationManager != null) {
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 60 * 1000, 50, this);
                    } else {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                } else {
                    Log.d("Location", "Location manager is null");
                }

            }
        } else {
            if (locationManager != null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 60 * 1000, 50, this);
            }
        }

    }


    @Override
    public void onLocationChanged(android.location.Location location) {
        //Log.d("Location", location.getLatitude() + ", " + location.getLongitude() + ", " + location.getAccuracy());
        Data data = new Data(StaticData.id, location.getLatitude(), location.getLongitude(), location.getAccuracy(), Calendar.getInstance().getTime());
        if (StaticData.userData.containsKey(StaticData.id)) {
            StaticData.userData.remove(StaticData.id);
        }
        StaticData.userData.put(StaticData.id, data);
        Azure azure = new Azure(context);
        azure.connect();
        azure.updateData(data);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
