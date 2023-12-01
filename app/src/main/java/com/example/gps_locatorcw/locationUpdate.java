package com.example.gps_locatorcw;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.GeofencingClient;

public class locationUpdate implements LocationListener {

    private TextView latitudeTextView;
    private TextView longitudeTextView;

    private Context context;


    public locationUpdate(Context context) {
        this.context = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update latitude and longitude TextViews with new values by their IDs
        TextView latitudeTextView = ((MainActivity) context).findViewById(R.id.latitude);
        TextView longitudeTextView = ((MainActivity) context).findViewById(R.id.longtitude);

        if (latitudeTextView != null && longitudeTextView != null) {
            latitudeTextView.setText("Latitude: " + location.getLatitude());
            longitudeTextView.setText("Longitude: " + location.getLongitude());
        }
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
