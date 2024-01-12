package com.example.gps_locatorcw.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class locationUpdate implements LocationListener {

    private TextView latitudeTextView;
    private TextView longitudeTextView;


    private Context context;
    private Location previousLocation;
    private double totalDistanceInMeters;

    private Location currentLocation;

    public locationUpdate(Context context) {
        this.context = context;
        previousLocation = null;
        totalDistanceInMeters = 0.0;
    }

    @Override
    public void onLocationChanged(Location location) {


        Log.d("LocationUpdate", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());



        if (previousLocation != null) {

            float distance = previousLocation.distanceTo(location);
            totalDistanceInMeters += distance;
        }


        previousLocation = location;
        currentLocation = location;



        double distanceInKilometers = getDistanceInKilometers();
    }


    public double getDistanceInKilometers(){
        double totalDistanceInKilometers = totalDistanceInMeters / 1000.0;
        return Math.round(totalDistanceInKilometers * 100.0) / 100.0;
    }

    public double getCurrentLatitude() {
        if (currentLocation != null) {
            return currentLocation.getLatitude();
        }
        return 0.0;
    }

    public double getCurrentLongitude() {
        if (currentLocation != null) {
            return currentLocation.getLongitude();
        }
        return 0.0;
    }


    public void resetDistance(){
        totalDistanceInMeters = 0.0;
    }

    public  double getDistance(){
        return totalDistanceInMeters;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        Log.d("comp3018", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {

        Log.d("comp3018", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {

        Log.d("comp3018", "onProviderDisabled: " + provider);
    }
}
