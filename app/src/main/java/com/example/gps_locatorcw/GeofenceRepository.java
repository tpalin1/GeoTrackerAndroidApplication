package com.example.gps_locatorcw;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class GeofenceRepository {
    private final GeofenceDAO geofenceDAO;
    private final LiveData<List<GeofenceStats>> allGeofences;

    public GeofenceRepository(GeofenceDAO geofenceDAO) {
        this.geofenceDAO = geofenceDAO;
        allGeofences = geofenceDAO.getAllGeofencesAsync();
    }

    public LiveData<List<GeofenceStats>> getAllGeofences() {
        return allGeofences;
    }

    public LiveData<List<GeofenceStats>> getAllGeofencesAsync() {
        return geofenceDAO.getAllGeofencesAsync();
    }


    public void insert(GeofenceStats geofence) {
        GeofenceDatabase.databaseWriteExecutor.execute(() -> {
            geofenceDAO.insert(geofence);
        });
    }

    public void insertAsync(GeofenceStats geofence) {
        GeofenceDatabase.databaseWriteExecutor.execute(() -> {
            geofenceDAO.insert(geofence);
        });
    }

    // Other methods for database operations if needed
}
