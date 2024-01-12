package com.example.gps_locatorcw.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.gps_locatorcw.utils.GeofenceBroadcastReceiver;
import com.example.gps_locatorcw.viewmodel.GeofenceViewModel;
import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.databases.DAO.GeofenceDAO;
import com.example.gps_locatorcw.databases.GeofenceDatabase;
import com.example.gps_locatorcw.databases.entities.GeofenceStats;
import com.example.gps_locatorcw.repos.GeofenceRepository;
import com.example.gps_locatorcw.services.LocationTrackingService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 123;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 1234;
    private GeofencingClient geofencingClient;
    private LocationTrackingService trackServ;
    private boolean isBound = false;
    private GeofenceDAO geofenceDAO;

    public GeofenceViewModel geofenceViewModel;
    private GeofenceDatabase geofenceDatabase;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();


    private GoogleMap mMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);


    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        geofencingClient = LocationServices.getGeofencingClient(requireActivity());
         geofenceViewModel = new ViewModelProvider(this).get(GeofenceViewModel.class);


        geofenceDatabase = GeofenceDatabase.getDatabase(requireContext());
        geofenceDAO = geofenceDatabase.geofenceDAO();


        geofenceViewModel = new ViewModelProvider(this).get(GeofenceViewModel.class);

        geofenceViewModel.init(new GeofenceRepository(geofenceDAO));
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps_fragmentView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);


        } else {
            mMap.setMyLocationEnabled(true);
            moveToCurrentUserLocation();

        }
    }



    private void moveToCurrentUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    mMap.setOnMyLocationChangeListener(null);
                }
            });
        }

    }






    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {


            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();

        mMap.setOnMapClickListener(this::onMapClick);
        loadSavedGeofences();





    }
    @Override
    public void onMapClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng));

        addCircle(latLng, 100);
    }


    private void addCircle(LatLng area, int radius) {

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(area);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.GREEN);
        circleOptions.fillColor(Color.RED);
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);


        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Reminder");
        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reminder = input.getText().toString().trim();
                if (!reminder.isEmpty()) {
                    String uniqueId = "UniqueId_" + area.latitude + "_" + area.longitude;










                    GeofenceStats geofenceStat = new GeofenceStats(uniqueId, reminder, area.latitude, area.longitude);
                    geofenceViewModel.insert(geofenceStat);

                    Geofence geofence = new Geofence.Builder()
                            .setRequestId(uniqueId)
                            .setCircularRegion(area.latitude, area.longitude, radius)
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                    Geofence.GEOFENCE_TRANSITION_EXIT |
                                    Geofence.GEOFENCE_TRANSITION_DWELL)
                            .setLoiteringDelay(1000)
                            .setNotificationResponsiveness(1000)
                            .build();

                    geofenceList.add(geofence);
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }


                    geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                            .addOnSuccessListener(requireActivity(), new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            })
                            .addOnFailureListener(requireActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });


                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
    private void loadSavedGeofences() {



            geofenceViewModel.getAllGeofencesAsync().observe(getViewLifecycleOwner(), new Observer<List<GeofenceStats>>() {
                @Override
                public void onChanged(List<GeofenceStats> geofenceStatsList) {
                    if (geofenceStatsList != null) {



                        for (GeofenceStats geofenceStats : geofenceStatsList) {
                            double latitude = geofenceStats.getLatitude();
                            double longitude = geofenceStats.getLongitude();
                            String reminder = geofenceStats.getReminder();

                            Log.d("Hdhdhdhdhdhdhdhdhdhd", "onChanged: " + reminder);

                            Geofence geofence = new Geofence.Builder()
                                    .setRequestId(geofenceStats.getGeofenceName())
                                    .setCircularRegion(latitude, longitude, 100)
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                            Geofence.GEOFENCE_TRANSITION_EXIT |
                                            Geofence.GEOFENCE_TRANSITION_DWELL)
                                    .setLoiteringDelay(2000)
                                    .build();

                            geofenceList.add(geofence);


                            LatLng geofenceLocation = new LatLng(latitude, longitude);
                            mMap.addMarker(new MarkerOptions().position(geofenceLocation));
                        }


                        reRegisterGeofences();
                    }
                }
            });

        }


    /**
     * This method is used to re-load geofences when the app is restarted.
     *
     */
    private void reRegisterGeofences() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || geofenceList.isEmpty()) {
            return;
        }

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(requireActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    /**
     * @return A GeofencingRequest object used to register geofences.
     * This method is used to create a GeofencingRequest object that will be used to register geofences.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        Log.d("Hebofdobf", "getGeofencingRequest: " + geofenceList.size());
        return builder.build();
    }

    /**
     * @return A PendingIntent that will handle geofence transitions.
     * This method is used to get a PendingIntent object that will be used to generate an intent when a geofence transition occurs.
     */
    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(requireContext(), GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        return geofencePendingIntent;
    }


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

    /**
     * This method is called when the fragment is visible to the user and actively running.
     */
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(requireContext(), LocationTrackingService.class);
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


}
