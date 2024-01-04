package com.example.gps_locatorcw;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class StatFragment extends Fragment implements OnMapReadyCallback{

    private LocationTrackingService locationService;

    private PolylineOptions polylineOptions;
    private Polyline polyline;

    private GeofenceDAO geofenceDAO;
    private GeofencingClient geofencingClient;


    public GeofenceViewModel geofenceViewModel;
    private GeofenceDatabase geofenceDatabase;
    private PendingIntent geofencePendingIntent;
    private List<Geofence> geofenceList = new ArrayList<>();




    private ServiceConnection serviceConnection;

    private LocationTrackingService trackServ;


    private int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 123;
    private StatsViewModel distanceViewModel;
    private boolean isBound = false;

    private GoogleMap mMap;

    private StatDatabase database;
    private StatDAO statDAO;

    private String activityType;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.stat_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button viewAllButton = view.findViewById(R.id.viewAll);
        viewAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGeofenceListDialog();
            }
        });

        geofencingClient = LocationServices.getGeofencingClient(requireActivity());
        geofenceViewModel = new ViewModelProvider(this).get(GeofenceViewModel.class);


        Bundle args = getArguments();
        if (args != null) {
            Log.d("bdoidfowebf", "onViewCreated: " + args.getString("activityType"));
             activityType = args.getString("activityType", ""); // Retrieve the activityType

        }

        // Initialize the database and GeofenceDAO
        geofenceDatabase = GeofenceDatabase.getDatabase(requireContext());
        geofenceDAO = geofenceDatabase.geofenceDAO();


        geofenceViewModel = new ViewModelProvider(this).get(GeofenceViewModel.class);

        geofenceViewModel.init(new GeofenceRepository(geofenceDAO));
        distanceViewModel = new ViewModelProvider(requireActivity()).get(StatsViewModel.class);


        // Observe the LiveData for exercise duration and average pace
        distanceViewModel.getExerciseDuration().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String duration) {
                TextView durationTextView = view.findViewById(R.id.timer);
                durationTextView.setText("Duration: " + duration); // Update duration TextView
            }
        });

        distanceViewModel.getAveragePace().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String pace) {
                TextView paceTextView = view.findViewById(R.id.avgpace);
                paceTextView.setText("Average Pace: " + pace + " per km"); // Update average pace TextView
            }
        });
        distanceViewModel.getDistanceTravelled().observe(requireActivity(), new Observer<Double>() {

            @Override
            public void onChanged(Double distanceTravelled) {

                TextView distanceTextView = view.findViewById(R.id.distanceTrav);
                distanceTextView.setText("Total distance travelled: " + distanceTravelled + " meters");
            }
        });

        // Display appropriate text based on the activity type received
//        switch (activityType) {
//            case "Cycle":
//                stateTextView.setText("On your current cycle you have:");
//                break;
//            case "Run":
//                stateTextView.setText("On your current run you have:");
//                break;
//            case "Walk":
//                stateTextView.setText("On your current walk you have:");
//                break;
//            default:
//                stateTextView.setText("Unknown activity");
//                break;
//        }
        // Your code to initialize views and set up UI components
        // Similar to what you did in onCreate of Stats activity

        // For example:
//        TextView stateTextView = view.findViewById(R.id.state);
//        stateTextView.setText("Your desired text here");      TextView stateTextView = view.findViewById(R.id.state);
//        stateTextView.setText("Your desired text here");

        Button stopBtn = view.findViewById(R.id.stopbtn);

        if(activityType!=null) {
            // Binding to LocationTrackingService
            stopBtn.setOnClickListener(v -> {
                promptExerciseName(activityType); // Prompt for exercise name when the button is clicked
                if (isBound) { // Check if the service is bound before unbinding
                    requireContext().unbindService(connection);
                    isBound = false;
                    if (locationService != null) {
                        locationService.stopTracking();
                    }
                }
            });

        }
            stopBtn.setBackgroundColor(Color.RED);



        Intent serviceIntent = new Intent(requireContext(), LocationTrackingService.class);


            requireContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

    }
    private void showGeofenceListDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.geofence_display_list);

        ListView geofenceListView = dialog.findViewById(R.id.geofenceListView);

        // Use your ViewModel to observe the geofence data changes
        geofenceViewModel.getAllGeofencesAsync().observe(getViewLifecycleOwner(), new Observer<List<GeofenceStats>>() {
            @Override
            public void onChanged(List<GeofenceStats> geofenceStatsList) {

                if (geofenceStatsList != null) {
                    List<String> geofenceNames = new ArrayList<>();
                    int index = 1;
                    for (GeofenceStats geofenceStats : geofenceStatsList) {
                        String reminder = geofenceStats.getReminder();
                        // Append index and reminder to the geofenceNames list
                        geofenceNames.add("Geofence " + index + " - " + reminder);
                        index++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, geofenceNames);
                    geofenceListView.setAdapter(adapter);
                }

            }
        });

        dialog.show();
    }

    // Implement this method to retrieve geofence names or details to display in the list
    private List<String> getGeofenceNames() {
        // Retrieve your geofence data here (e.g., from Room database or ViewModel)
        // Return a list of geofence names or details
        // For example:
        List<String> geofenceNames = new ArrayList<>();
        // Add geofence names to the list
        // geofenceNames.add("Geofence 1");
        // geofenceNames.add("Geofence 2");
        // ...
        return geofenceNames;
    }
    // Add a method to prompt an input dialog for the exercise name
    private void promptExerciseName(String activityType) {
        // Use an AlertDialog or DialogFragment to prompt for exercise name
        // For example:
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter Exercise Name");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String exerciseName = input.getText().toString().trim();
                if (!exerciseName.isEmpty()) {
                    Log.d("Here is the exercise", exerciseName);
                    database = StatDatabase.getDatabase(locationService.getApplicationContext());
                    statDAO = database.statDAO();

                    // Assuming you have an instance of your DAO (statDAO) available in the service
                    List<double[]> coordinates = locationService.getCoordinatesArray();

                    // Use Kotlin coroutines to perform database operations off the main thread
                    StatDatabase.databaseWriteExecutor.execute(() -> {
                        ExerciseStats exerciseStats = new ExerciseStats(exerciseName, activityType, distanceViewModel.getDistanceTravelled().getValue(),coordinates, distanceViewModel.getExerciseDuration().getValue(), distanceViewModel.getAveragePace().getValue());
                        statDAO.insert(exerciseStats); // Assuming you have an insert method in your DAO

                        // Fetch all ExerciseStats entries from the database asynchronously
                        List<ExerciseStats> allStats = statDAO.getAllCats(); // This line should also be executed inside a coroutine

                        // Log the fetched entries
                        for (ExerciseStats stats : allStats) {
                            Log.d("ExerciseStats", "Exercise: " + stats.getExercise() + ", Duration: " + stats.getDuration());
                        }
                    });




                } else {
                    // Show an error or handle empty exercise name
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


    // Add a method to enable user location




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
                    updatePolyline(location); // Call to update the polyline with user's location change
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
                // Permission denied
                // Handle this scenario as needed
            }
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        loadSavedGeofences(); // Load saved geofences from the database
        initializePolylineOptions(); // Initialize PolylineOptions



        // Customize the map as needed
        // For example:
        // mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Add markers, set initial camera position, or perform other map-related operations here
    }

    // Method to initialize PolylineOptions
    private void initializePolylineOptions() {
        polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(10f);
        // You can set other properties of the polyline here
    }



    private void updatePolyline(Location location) {
        if (polylineOptions != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            polylineOptions.add(latLng);

            if (polyline != null) {
                polyline.remove(); // Remove previous polyline
            }
            polyline = mMap.addPolyline(polylineOptions); // Draw the updated polyline on the map
        }
    }


    private void loadSavedGeofences() {
        // Retrieve geofences from the Room database asynchronously using LiveData


        geofenceViewModel.getAllGeofencesAsync().observe(getViewLifecycleOwner(), new Observer<List<GeofenceStats>>() {
            @Override
            public void onChanged(List<GeofenceStats> geofenceStatsList) {
                if (geofenceStatsList != null) {
                    // Clear the map before adding new markers


                    for (GeofenceStats geofenceStats : geofenceStatsList) {
                        double latitude = geofenceStats.getLatitude();
                        double longitude = geofenceStats.getLongitude();
                        String reminder = geofenceStats.getReminder();

                        Log.d("Hdhdhdhdhdhdhdhdhdhd", "onChanged: " + reminder);
                        // Create a Geofence object using retrieved data
                        Geofence geofence = new Geofence.Builder()
                                .setRequestId(geofenceStats.getGeofenceName())
                                .setCircularRegion(latitude, longitude, 100) // Set your radius here
                                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                        Geofence.GEOFENCE_TRANSITION_EXIT |
                                        Geofence.GEOFENCE_TRANSITION_DWELL)
                                .setLoiteringDelay(2000)
                                .build();

                        geofenceList.add(geofence);

                        // Add marker for each geofence
                        LatLng geofenceLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(geofenceLocation));
                    }

                    // Register the retrieved geofences
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
                        // Geofences added successfully
                    }
                })
                .addOnFailureListener(requireActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add Geofences
                    }
                });
    }
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        Log.d("Hebofdobf", "getGeofencingRequest: " + geofenceList.size());
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



    // Update the TextView showing the distance travelled
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationTrackingService.LocalBinder binder = (LocationTrackingService.LocalBinder) service;
            locationService = binder.getService();
            isBound = true;
            locationService.setCallback(progress -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getView() != null) {
                            updateDistanceTextView(); // Update UI only if the fragment's view is available
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


    // Update the TextView showing the distance travelled
    private void updateDistanceTextView() {


        if (isBound && locationService != null) {


            distanceViewModel.updateDistance(locationService.getDistanceTravelled());
            distanceViewModel.updateExerciseDuration(locationService.getDuration());
            distanceViewModel.updateAveragePace(locationService.getPace());


        }
    }




}
