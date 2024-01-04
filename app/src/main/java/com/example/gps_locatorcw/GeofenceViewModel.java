package com.example.gps_locatorcw;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
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
}
