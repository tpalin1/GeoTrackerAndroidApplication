package com.example.gps_locatorcw.databases.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "geofence_table")
public class GeofenceStats {

    @PrimaryKey(autoGenerate = true)
    private int geofenceId;


    @ColumnInfo(name = "reminder")
    private String reminder;
    @NonNull
    @ColumnInfo(name = "geofence_name")
    private String geofenceName;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;



    public GeofenceStats(@NonNull String geofenceName, String reminder, double latitude, double longitude) {
        this.geofenceName = geofenceName;
        this.reminder = reminder;
        this.latitude = latitude;
        this.longitude = longitude;
    }



    public int getGeofenceId() {
        return geofenceId;
    }

    public void setGeofenceId(int geofenceId) {
        this.geofenceId = geofenceId;
    }


    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }
    @NonNull
    public String getGeofenceName() {
        return geofenceName;
    }

    public void setGeofenceName(@NonNull String geofenceName) {
        this.geofenceName = geofenceName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
