package com.example.gps_locatorcw.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.gps_locatorcw.databases.DAO.GeofenceDAO;
import com.example.gps_locatorcw.databases.entities.GeofenceStats;
import com.example.gps_locatorcw.repos.GeofenceRepository;

import java.util.List;

public class GeofenceViewModel extends ViewModel {
    private GeofenceRepository repository;
    private LiveData<List<GeofenceStats>> allGeofences;

    private GeofenceDAO geofenceDAO;

    public GeofenceViewModel() {
    }

    public void init(GeofenceRepository repo) {
        this.repository = repo;
        allGeofences = repository.getAllGeofencesAsync();
    }

    public LiveData<List<GeofenceStats>> getAllGeofencesAsync() {
        return allGeofences;
    }

    public void insert(GeofenceStats geofence) {
        repository.insertAsync(geofence);
    }

    public void delete(GeofenceStats geofence){
        repository.delete(geofence);
    }
}
