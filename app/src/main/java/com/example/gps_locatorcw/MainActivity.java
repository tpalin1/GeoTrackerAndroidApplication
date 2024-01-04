package com.example.gps_locatorcw;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 1234;
    private LocationManager locationManager;
    private locationUpdate locationListener;

    private LocationTrackingService trackServ;

    private boolean isBound = false;
    private GeofencingClient geofencingClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView toMapsButton = findViewById(R.id.toMaps);

        ImageView homeButton = findViewById(R.id.homeBtn); // Assuming you have a home button
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                onBackPressed();
            }
        });
        toMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // Explain the need for background location access
                    showPermissionExplanationDialog();
                    // Replace the current fragment with MapFragment

                }getSupportFragmentManager().beginTransaction()


                        .replace(R.id.map_fragment_container, new MapFragment())
                        .addToBackStack(null)
                        .commit();

            }
            });
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                123);

        // Start the service when a music item is clicked
        Intent serviceIntent = new Intent(MainActivity.this, LocationTrackingService.class);

        startService(serviceIntent);

        // Bind to the service
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
//        Button cycleButton = findViewById(R.id.Cycle);
        Button runButton = findViewById(R.id.run);
        Button walkButton = findViewById(R.id.cycle);
        ImageView activityBtn = findViewById(R.id.activitybtn);
//        cycleButton.setOnClickListener(v -> openStatsFragment("Cycle"));
        runButton.setOnClickListener(v -> openStatsFragment("Run"));
        walkButton.setOnClickListener(v -> openStatsFragment("Walk"));
        activityBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatPage.class);
            startActivity(intent);
        });
        requestPermissionsIfNecessary();
    }

    // Method to display a dialog explaining the need for background location access
    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Background Location Access");
        builder.setMessage("To create reminders and to use the application properly please enable 'always allow'.");

        builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Request background location permission
                requestBackgroundLocationPermission();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    // Method to request background location permission
    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Signal the service about app going into the background
        if (isBound && trackServ != null) {
            trackServ.onAppInBackground();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Signal the service about app returning to the foreground
        if (isBound && trackServ != null) {
            trackServ.onAppInForeground();
        }
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack(); // Close the current fragment
        } else {
            super.onBackPressed(); // If no fragments in back stack, handle default behavior
        }
    }
    private void openStatsFragment(String activityType) {

        // Replace the current fragment with MapFragment
        getSupportFragmentManager().beginTransaction()
                //Open the statFragment fragment
                .replace(R.id.map_fragment_container, new StatFragment()).addToBackStack(null).commit();
    }

    private void requestPermissionsIfNecessary() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions,
                    123);
        }
    }
    // Open StatsActivity and pass the activity type as an extra

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationTrackingService.LocalBinder binder = (LocationTrackingService.LocalBinder) service;
            trackServ = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };







}