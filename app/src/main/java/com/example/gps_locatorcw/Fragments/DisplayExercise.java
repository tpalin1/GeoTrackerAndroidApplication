package com.example.gps_locatorcw.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gps_locatorcw.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class DisplayExercise extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 123;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps_fragmentView);
        mapFragment.getMapAsync(this);


    }


    /**
     * @param coordinates The coordinates of the route the user took
     *                    Map the route they took onto the map
     *
     */
    //Map the route they took
    private void drawPolyline(List<double[]> coordinates) {
        PolylineOptions polylineOptions = new PolylineOptions();

        Log.d("coordinates", String.valueOf(coordinates));

        for (double[] coordinate : coordinates) {
            double latitude = coordinate[0];
            double longitude = coordinate[1];


            if (latitude == 0.0 && longitude == 0.0) {
                continue;
            }

            polylineOptions.add(new LatLng(latitude, longitude));
        }

        mMap.addPolyline(polylineOptions);


        if (!polylineOptions.getPoints().isEmpty()) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : polylineOptions.getPoints()) {
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();


            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 200);


            mMap.moveCamera(cameraUpdate);
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
            mMap.setMyLocationEnabled(true);
            mMap.setMyLocationEnabled(true);
        }
    }


    /**
     * Move the camera to the zoomed in map route that the user took
     */
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
    public void onMapClick(@NonNull LatLng latLng) {

    }

    /**
     * @param googleMap
     * When the map is ready, draw the polyline
     * and then make thre user able to move around the mapo and stuff
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            List<double[]> coordinates = (List<double[]>) bundle.getSerializable("coordinates");
            if (coordinates != null) {
                drawPolyline(coordinates);
            }
        }

    }





    }
