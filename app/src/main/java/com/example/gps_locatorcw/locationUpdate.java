package com.example.gps_locatorcw;

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
    private Location previousLocation; // Variable to store the previous location
    private double totalDistanceInMeters; // Variable to store total distance travelled

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
            // Calculate distance between current and previous location and update total distance
            float distance = previousLocation.distanceTo(location);
            totalDistanceInMeters += distance;
        }

        // Update previousLocation with the current location for the next calculation
        previousLocation = location;
        currentLocation = location;

        // You can pass or use totalDistanceInMeters as needed here
        // For example, update UI or pass it to a service
        double distanceInKilometers = getDistanceInKilometers();
    }


    public double getDistanceInKilometers(){
        double totalDistanceInKilometers = totalDistanceInMeters / 1000.0;
        return Math.round(totalDistanceInKilometers * 100.0) / 100.0; // Rounds to two decimal places
    }

    public double getCurrentLatitude() {
        if (currentLocation != null) {
            return currentLocation.getLatitude();
        }
        return 0.0; // Or handle null case according to your needs
    }

    public double getCurrentLongitude() {
        if (currentLocation != null) {
            return currentLocation.getLongitude();
        }
        return 0.0; // Or handle null case according to your needs
    }


    public  double getDistance(){
        return totalDistanceInMeters;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle changes in GPS status
        Log.d("comp3018", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // GPS provider enabled
        Log.d("comp3018", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // GPS provider disabled
        Log.d("comp3018", "onProviderDisabled: " + provider);
    }
}
