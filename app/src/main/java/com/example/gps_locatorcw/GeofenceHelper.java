package com.example.gps_locatorcw;

import android.content.Context;
import android.content.ContextWrapper;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class GeofenceHelper extends ContextWrapper {

    public GeofenceHelper(Context base) {
        super(base);
    }

    public Geofence createGeofence(LatLng latLng, float radius, int transitionTypes) {
        return new Geofence.Builder()
                .setRequestId(generateRequestId())
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(transitionTypes)
                .build();
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}