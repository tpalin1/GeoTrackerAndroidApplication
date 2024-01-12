package com.example.gps_locatorcw.repos;

import androidx.lifecycle.LiveData;

import com.example.gps_locatorcw.databases.DAO.GeofenceDAO;
import com.example.gps_locatorcw.databases.GeofenceDatabase;
import com.example.gps_locatorcw.databases.entities.GeofenceStats;

import java.util.List;

public class GeofenceRepository {
    private final GeofenceDAO geofenceDAO;
    private final LiveData<List<GeofenceStats>> allGeofences;

    public GeofenceRepository(GeofenceDAO geofenceDAO) {
        this.geofenceDAO = geofenceDAO;
        allGeofences = geofenceDAO.getAllGeofencesAsync();
    }


    public LiveData<List<GeofenceStats>> getAllGeofencesAsync() {
        return geofenceDAO.getAllGeofencesAsync();
    }


    public void insert(GeofenceStats geofence) {
        GeofenceDatabase.databaseWriteExecutor.execute(() -> {
            geofenceDAO.insert(geofence);
        });
    }

    public void delete(GeofenceStats geofence){
        GeofenceDatabase.databaseWriteExecutor.execute(() -> {
            geofenceDAO.delete(geofence);
        });
    }


    public void insertAsync(GeofenceStats geofence) {
        GeofenceDatabase.databaseWriteExecutor.execute(() -> {
            geofenceDAO.insert(geofence);
        });
    }


}
