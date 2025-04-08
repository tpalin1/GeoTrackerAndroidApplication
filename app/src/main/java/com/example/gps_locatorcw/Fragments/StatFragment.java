package com.example.gps_locatorcw.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.gps_locatorcw.viewmodel.PolylineViewModel;
import com.example.gps_locatorcw.databinding.FragmentStatBinding;
import com.example.gps_locatorcw.utils.GeofenceBroadcastReceiver;
import com.example.gps_locatorcw.viewmodel.GeofenceViewModel;
import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.viewmodel.StatsViewModel;
import com.example.gps_locatorcw.databases.DAO.GeofenceDAO;
import com.example.gps_locatorcw.databases.DAO.StatDAO;
import com.example.gps_locatorcw.databases.GeofenceDatabase;
import com.example.gps_locatorcw.databases.StatDatabase;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private LocationTrackingService locationService;

    private PolylineOptions polylineOptions;
    private Polyline polyline;

    private SupportMapFragment mapFragment;
    private GeofenceDAO geofenceDAO;
    private GeofencingClient geofencingClient;


    public GeofenceViewModel geofenceViewModel;
    private GeofenceDatabase geofenceDatabase;

    private Map<String, Marker> geofenceMarkers = new HashMap<>();

    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();




    private ServiceConnection serviceConnection;

    private LocationTrackingService trackServ;


    private int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 123;
    private boolean isBound = false;

    private GoogleMap mMap;

    private StatDatabase database;
    private StatDAO statDAO;

    private String activityType;

    private PolylineViewModel polylineViewModel;
    StatsViewModel distanceViewModel;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentStatBinding binding = FragmentStatBinding.inflate(inflater, container, false);

        // Restore state from savedInstanceState
        if (savedInstanceState != null) {
            polylineOptions = savedInstanceState.getParcelable("polylineOptions");
            // Other state restoration...
        }

        distanceViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);
        binding.setViewModel(distanceViewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.stat_fragment);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.stat_fragment, mapFragment)
                    .commit();
            mapFragment.getMapAsync(this);
        } else {
            mapFragment.getMapAsync(this);
        }

        Bundle args = getArguments();
        if (args != null) {
            boolean displayDialogButton = args.getBoolean("displayDialogButton", false);

            if (displayDialogButton) {


                promptExerciseName("Run");
            }
        }

        Button viewAllButton = view.findViewById(R.id.viewAll);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGeofenceListDialog();
            }
        });



        Button stopBtn = view.findViewById(R.id.stopbtn);

        Button startButton = view.findViewById(R.id.startExercise);





        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationService.hasStarted()) {
                    locationService.startExercise();
                    moveToCurrentUserLocation();
                    startButton.setVisibility(View.GONE);
                    distanceViewModel.setIsExercising(true);
                    stopBtn.setVisibility(View.VISIBLE);
                }
            }
        });



        geofencingClient = LocationServices.getGeofencingClient(requireActivity());
        geofenceViewModel = new ViewModelProvider(this).get(GeofenceViewModel.class);


        if (args != null) {
             activityType = args.getString("activityType", "");

        }


        geofenceDatabase = GeofenceDatabase.getDatabase(requireContext());
        geofenceDAO = geofenceDatabase.geofenceDAO();

        polylineViewModel = new ViewModelProvider(this).get(PolylineViewModel.class);



        geofenceViewModel = new ViewModelProvider(this).get(GeofenceViewModel.class);

        geofenceViewModel.init(new GeofenceRepository(geofenceDAO));



        polylineViewModel.getPolylinePoints().observe(getViewLifecycleOwner(), new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> polylinePoints) {
                Log.d("dsabodab", "Polylines printed"+ polylinePoints.toString());

                updatePolylineOnMap(polylinePoints);
            }
        });

        if(activityType!=null) {

            stopBtn.setOnClickListener(v -> {
                promptExerciseName(activityType);
                if (isBound) {
                    requireContext().unbindService(connection);
                    isBound = false;
                    if (locationService != null) {
                        Log.d("Cancelled notifiation", "Service unbinded: ");
                        locationService.stopExercise();
                        distanceViewModel.setIsExercising(false);


                    }
                }

            });


        }
            stopBtn.setBackgroundColor(Color.RED);



        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);


            requireContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }


    @Override
    public void onMapClick(LatLng latLng) {



        addCircle(latLng, 100);
    }

    private void removeMarker(String geofenceId) {
        if (geofenceMarkers.containsKey(geofenceId)) {
            Marker marker = geofenceMarkers.get(geofenceId);

            marker.remove();

            Log.d("Removed", "sadobiboais");
            geofenceMarkers.remove(geofenceId);
            mMap.clear();
            reRegisterGeofences();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("polylineOptions", polylineOptions);
    }


    private void addCircle(LatLng area, int radius) {

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(area);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.GREEN);
        circleOptions.fillColor(Color.RED);
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);


        Marker marker = mMap.addMarker(new MarkerOptions().position(area));


        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter your reminder");
        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reminder = input.getText().toString().trim();
                if (!reminder.isEmpty()) {
                    String uniqueId = "UniqueId_" + area.latitude + "_" + area.longitude;
                    Log.d("Hdhdhdhdhdhdhdhdhdhd", "onChanged: " + reminder);
                    geofenceMarkers.put(uniqueId, marker);









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

                    Log.d("Hdhdhdhdhdhdhdhdhdhd", " " + geofence.getRequestId());
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


    /**
     * This method is used to show a dialog containing a list of all geofences in the database.
     * The geofences are retrieved from the Room database asynchronously using LiveData.
     * The geofence names are displayed in a ListView.
     * It has an X next to their name so that the user can delete the geofence from the database.
     */



    private void showGeofenceListDialog() {
        Dialog dialog = new Dialog(requireContext());
        Log.d("dnpdfewnf", "Entered");
        dialog.setContentView(R.layout.geofence_display_list);

        ListView geofenceListView = dialog.findViewById(R.id.geofenceListView);


        geofenceViewModel.getAllGeofencesAsync().observe(getViewLifecycleOwner(), new Observer<List<GeofenceStats>>() {
            @Override
            public void onChanged(List<GeofenceStats> geofenceStatsList) {
                if (geofenceStatsList != null) {
                    List<String> geofenceNames = new ArrayList<>();
                    int index = 1;
                    for (GeofenceStats geofenceStats : geofenceStatsList) {
                        geofenceNames.add("Geofence " + index + " - " + geofenceStats.getReminder());
                        index++;
                    }


                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(), R.layout.geofence_list_design, R.id.geofenceNameTextView, geofenceNames) {
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);


                            ImageView deleteButton = view.findViewById(R.id.deleteButton);
                            deleteButton.setTag(position);
                            deleteButton.setOnClickListener(StatFragment.this::onDeleteButtonClick);
                            return view;
                        }
                    };
                    geofenceListView.setAdapter(adapter);
                }
            }
        });

        dialog.show();
    }

    public void onDeleteButtonClick(View view) {
        int position = (int) view.getTag();


        if (geofenceViewModel != null) {

            List<GeofenceStats> geofenceStatsList = geofenceViewModel.getAllGeofencesAsync().getValue();

            if (geofenceStatsList != null && position < geofenceStatsList.size()) {
                GeofenceStats geofenceStats = geofenceStatsList.get(position);

                removeGeofence(geofenceStats);
            }
        }
    }




    private void removeGeofence(GeofenceStats geofenceStats) {

        geofenceList.removeIf(geofence -> geofence.getRequestId().equals(geofenceStats.getGeofenceName()));


        deleteGeofence(geofenceStats);
        removeMarker(geofenceStats.getGeofenceName());

    }


    public void deleteGeofence(GeofenceStats geofenceStats) {
        geofenceViewModel.delete(geofenceStats);
    }


    /**
     * @param activityType The type of activity (e.g. Run, Walk, Cycle)
     *This method is used to prompt an input dialog for the exercise name.
     *The exercise name is used as the primary key for the ExerciseStats entity.
     */

    private void promptExerciseName(String activityType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Exercise Name");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save Exercise", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                locationService.stopExercise();

                String exerciseName = input.getText().toString().trim();
                if (exerciseName.isEmpty()) {
                   dialog.cancel();
                }



                Log.d("Here is the exercise", exerciseName);

                database = StatDatabase.getDatabase(locationService.getApplicationContext());
                statDAO = database.statDAO();



                List<double[]> coordinates = locationService.getCoordinatesArray();

                StatDatabase.databaseWriteExecutor.execute(() -> {
                    ExerciseStats exerciseStats = new ExerciseStats(exerciseName, activityType, distanceViewModel.getDistanceTravelled().getValue(), coordinates, distanceViewModel.getExerciseDuration().getValue(), distanceViewModel.getAveragePace().getValue());
                    statDAO.insert(exerciseStats);

                    List<ExerciseStats> allStats = statDAO.getAllCats();

                    for (ExerciseStats stats : allStats) {
                        Log.d("ExerciseStats", "Exercise: " + stats.getExercise() + ", Duration: " + stats.getDuration());
                    }
                });
                if (requireActivity().getSupportFragmentManager() != null) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }



            }
        });

        builder.setNegativeButton("Dont save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (requireActivity().getSupportFragmentManager() != null) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
                dialog.cancel();
            }
        });


        builder.show();
    }





    /**
     * This method is used to enable the user's location on the map.
     * It also moves the camera to the user's current location.
     * If the user's location permission is not granted, it requests for the permission.
     */
private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            //Request Permissions for everything needed for geofencing and tracking
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);
        } else {

            //So they can navigate throguh the app
            mMap.setMyLocationEnabled(true);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);


        }
    }


    /**
     * This method is used to move the camera to the user's current location.
     * It also updates the polyline with the user's location change.
     */
    private void moveToCurrentUserLocation() {

            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                }
            });

        };



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

    /**
     * This method is used to remove the observers when the fragment is destroyed.
     * It also removes the mapFragment to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        geofenceViewModel.getAllGeofencesAsync().removeObservers(getViewLifecycleOwner());


        if (mapFragment != null) {
            getChildFragmentManager().beginTransaction()
                    .remove(mapFragment)
                    .commitAllowingStateLoss();
        }
    }


    /**
     * @param googleMap The GoogleMap object
     * This method is used to initialize the GoogleMap object.
     * It also sets the OnMapClickListener and enables the user's location on the map.
     * It also loads the saved geofences from the database and registers them.
     *It also initializes the PolylineOptions object.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        // Check if the user has started an exercise
        if (distanceViewModel.getIsExercising().getValue() != null && distanceViewModel.getIsExercising().getValue()) {
            // User has started an exercise, proceed with enabling features

            moveToCurrentUserLocation();
            enableUserLocation();
            loadSavedGeofences();

            initializePolylineOptions();

        }
        //If they havent started a
        mMap.setOnMapClickListener(this);

        Log.d("Helli there", "sdabdasobdobsabiodsaipo");

        enableUserLocation();
        loadSavedGeofences();
        initializePolylineOptions();









    }

    /**
     * This method is used to initialize the PolylineOptions object.
     * It also sets the color and width of the polyline.
     */

    private void initializePolylineOptions() {

            // Initialize polylineOptions if not restored from savedInstanceState
            if (polylineOptions == null) {
                polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.BLUE);
                polylineOptions.width(10f);
            }


    }




    /**
     * This method is used to load the saved geofences from the Room database.
     * It also adds markers for each geofence and registers them and gets the saved reminder frm it
     * It also removes the geofence from the database once it is triggered.
     */
    private void loadSavedGeofences() {



        geofenceList.clear();
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

    public void updatePolylineOnMap(List<LatLng> polylinePoints) {
        if(mMap == null) {
            return;
        }
        if (polyline == null) {

            polylineOptions.addAll(polylinePoints);
            polyline = mMap.addPolyline(polylineOptions);
        } else {

            polyline.setPoints(polylinePoints);
        }
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

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
            locationService = binder.getService();
            isBound = true;
            if(locationService.hasStarted() && !locationService.isPaused()){
                if(getView() != null){
                    getView().findViewById(R.id.startExercise).setVisibility(View.GONE);
                    getView().findViewById(R.id.stopbtn).setVisibility(View.VISIBLE);
                }

            }
            locationService.setCallback(progress -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getView() != null) {
                            updateDistanceTextView();
                        }
                    });
                }
            });
            }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }


    };





    private void updateDistanceTextView() {


        if (isBound && locationService != null) {


            distanceViewModel.updateDistance(locationService.getDistanceTravelled());
            distanceViewModel.updateExerciseDuration(locationService.getDuration());
            distanceViewModel.updateAveragePace(locationService.getPace());

            polylineViewModel.updatePolyline(locationService.getLocationList());


        }
    }




}
