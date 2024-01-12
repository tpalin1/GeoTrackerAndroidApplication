package com.example.gps_locatorcw.databases.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gps_locatorcw.databases.entities.GeofenceStats;

import java.util.List;

@Dao
public interface GeofenceDAO {

    /**
     * @param geofence
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(GeofenceStats geofence);


    /**
     * @param geofence
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAsync(GeofenceStats geofence);

    @Query("SELECT * FROM geofence_table")
    List<GeofenceStats> getAllGeofences();

    /**
     * @param geofence
     */
    @Update
    void updateGeofence(GeofenceStats geofence);

    /**
     * @param geofenceStats
     */
    @Delete
    void delete(GeofenceStats geofenceStats);

    /**
     * @param id
     */
    @Query("DELETE FROM geofence_table WHERE geofenceId = :id")
    void deleteGeofenceById(int id);

    @Query("DELETE FROM geofence_table")
    void deleteAllGeofences();

    @Query("SELECT * FROM geofence_table WHERE geofenceId = :id")
    LiveData<GeofenceStats> getGeofenceById(int id);

    @Query("SELECT * FROM geofence_table")
    LiveData<List<GeofenceStats>> getAllGeofencesAsync();
}
