package com.example.gps_locatorcw;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GeofenceDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(GeofenceStats geofence);



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAsync(GeofenceStats geofence);

    @Query("SELECT * FROM geofence_table")
    List<GeofenceStats> getAllGeofences();

    @Update
    void updateGeofence(GeofenceStats geofence);

    @Query("DELETE FROM geofence_table WHERE geofenceId = :id")
    void deleteGeofenceById(int id);

    @Query("DELETE FROM geofence_table")
    void deleteAllGeofences();

    @Query("SELECT * FROM geofence_table WHERE geofenceId = :id")
    LiveData<GeofenceStats> getGeofenceById(int id);

    @Query("SELECT * FROM geofence_table")
    LiveData<List<GeofenceStats>> getAllGeofencesAsync();
}
