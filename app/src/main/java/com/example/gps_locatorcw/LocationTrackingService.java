package com.example.gps_locatorcw;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.LocationServices;

public class LocationTrackingService extends Service {


    public LocationTrackingService() {
    }

    private static final String CHANNEL_ID = "Tracking";


    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private Notification notification;
    private final IBinder binder = new LocalBinder();

    private LocationManager locationManager;
    private static final int NOTIF_ID = 1;

    private locationUpdate locationListener;


    public void onCreate() {
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        // ... (other code)




        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);




        //Start the notification

        // Pass notification components to locationUpdate
        locationListener = new locationUpdate(this);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, // Minimum time interval between updates (in milliseconds)
                    5, // Minimum distance between updates (in meters)
                    locationListener);
        } catch (SecurityException e) {
            Log.d("comp3018", e.toString());
        }

        return START_STICKY;
    }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }





    public class LocalBinder extends Binder {
        LocationTrackingService getService() {
            return LocationTrackingService.this;
        }
    }

}