package com.example.gps_locatorcw;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GeofenceHelper extends ContextWrapper {

    PendingIntent pendingIntent;
    private final List<String> geofenceIds = new ArrayList<>(); // Store generated IDs

    public GeofenceHelper(Context base) {
        super(base);
    }

    public Geofence createGeofence(LatLng latLng, float radius, int transitionTypes) {
        String uniqueId = generateUniqueId(); // Generate a unique ID for the geofence
        geofenceIds.add(uniqueId); // Add the ID to the list for future reference

        return new Geofence.Builder()
                .setRequestId(uniqueId) // Set the unique ID as the request ID for the geofence
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(transitionTypes)
                .build();
    }

    private String generateUniqueId() {
        // Generate a unique ID using UUID
        return UUID.randomUUID().toString();
    }

    public GeofencingRequest getGeoReq(Geofence geo){

        return new GeofencingRequest.Builder().addGeofence(geo)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }


    public PendingIntent getPendingIntent(){
        if(pendingIntent !=null){
            return pendingIntent;
        }
        Intent intent=new Intent(this, GeofenceBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 1234, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent;
    }


}