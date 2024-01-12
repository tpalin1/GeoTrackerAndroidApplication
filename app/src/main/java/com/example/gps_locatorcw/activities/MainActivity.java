package com.example.gps_locatorcw.activities;

import static java.util.Calendar.getInstance;

import com.example.gps_locatorcw.Fragments.StatFragment;
import com.example.gps_locatorcw.Fragments.StatPage;
import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.viewmodel.GeofenceViewModel;
import com.example.gps_locatorcw.utils.locationUpdate;
import com.example.gps_locatorcw.services.LocationTrackingService;
import com.google.android.gms.location.GeofencingClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.Context;
import android.location.LocationManager;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {


    private static final int MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 1234;
    private LocationManager locationManager;
    private locationUpdate locationListener;

    private LocationTrackingService trackServ;

    private boolean isBound = false;
    private GeofencingClient geofencingClient;

    private boolean hasStarted = false;

    private boolean isActivityStart = true;

    private static final String KEY_STARTED_STATE = "started_state";


    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     * Creates all of the buttons and sets the onClickListeners for each of them
     * Also checks if the user has the background location permission and if not, asks for it
     *

     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean hasOpenedUpMap = getIntent().getBooleanExtra("hasOpenedUpMap", false);


        boolean loadMapFragment = getIntent().getBooleanExtra("loadMapFragment", false);
        boolean displayDialogButton = getIntent().getBooleanExtra("displayDialogButton", false);



        if (savedInstanceState != null) {
            hasStarted = savedInstanceState.getBoolean(KEY_STARTED_STATE);

        }

        if (loadMapFragment) {
            StatFragment statFragment = new StatFragment();

            Bundle args = new Bundle();
            args.putBoolean("displayDialogButton", displayDialogButton);

            statFragment.setArguments(args);

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.map_fragment_container, statFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();


        }
        if(hasOpenedUpMap){
            isActivityStart = false;
        }
        ImageView toMapsButton = findViewById(R.id.toMaps);
        FrameLayout statActivityFrameLayout = findViewById(R.id.Stat_Activity);
        FrameLayout homeActivity = findViewById(R.id.map_fragment_container);
        ImageView homeButton = findViewById(R.id.homeBtn);


        homeButton.setOnClickListener(new View.OnClickListener() {
            /**
             * @param v The view that was clicked.
             *
             *         Navigates back to the main activity
             */
            @Override
            public void onClick(View v) {

                onBackPressed();
                homeActivity.bringToFront();
            }
        });

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                123);




        Intent serviceIntent = new Intent(MainActivity.this, LocationTrackingService.class);
        startService(serviceIntent);

        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        Button runButton = findViewById(R.id.run);
        Button walkButton = findViewById(R.id.walks);
        Button cycleButton = findViewById(R.id.cycle);


        ImageView activityBtn = findViewById(R.id.activitybtn);

        runButton.setOnClickListener(v -> openStatsFragment("Run"));
        walkButton.setOnClickListener(v -> openStatsFragment("Walk"));
        cycleButton.setOnClickListener(v -> openStatsFragment("Cycle"));

        activityBtn.setOnClickListener(v -> {

            StatPage statPage = new StatPage();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.Stat_Activity, statPage)
                    .addToBackStack(null)
                    .commit();
            statActivityFrameLayout.bringToFront();
        });


        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionExplanationDialog();

        }


    }

    /**
     * @param outState Bundle in which to place your saved state.
     *                 Saves the state of hasStarted in the savedInstanceState bundle
     * In case the user goes back into an activity or rottates the screen
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_STARTED_STATE, hasStarted);
    }

    /**
     * Shows the dialog explaining the need for background location access
     */

    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Background Location Access");
        builder.setMessage("To create reminders and activate them when youre not in the app and to use the application properly please enable 'always allow'.");

        builder.setPositiveButton("Grant Permission", (dialog, which) -> requestBackgroundLocationPermission());

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }


    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isBound && trackServ != null) {
            trackServ.onAppInBackground();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isBound && trackServ != null) {
            trackServ.onAppInForeground();
        }
    }
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            FrameLayout homeActivity = findViewById(R.id.map_fragment_container);
            homeActivity.bringToFront();
        } else {
            super.onBackPressed();
        }
    }
    private void openStatsFragment(String activityType) {

        StatFragment statFragment = new StatFragment();
        Bundle args = new Bundle();
        args.putString("activityType", activityType);
        args.putBoolean("displayDialogButton", false);
        statFragment.setArguments(args);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_fragment_container, statFragment)
                .addToBackStack(null)
                .commit();







    }
    private GeofenceViewModel geofenceViewModel;




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