package com.example.gps_locatorcw;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {


    private LocationManager locationManager;
    private locationUpdate locationListener;
    private GeofencingClient geofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button toMapsButton = findViewById(R.id.toMaps);
        toMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the MapsActivity when the button is clicked
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        geofencingClient = LocationServices.getGeofencingClient(this);


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                123);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new locationUpdate(this);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, // Minimum time interval between updates (in milliseconds)
                    5, // Minimum distance between updates (in meters)
                    locationListener);


        } catch (SecurityException e) {
            Log.d("comp3018", e.toString());
        }


















    }



}