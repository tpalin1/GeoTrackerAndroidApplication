package com.example.gps_locatorcw;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationTrackingService extends Service {


    public LocationTrackingService() {
    }

    private long foregroundUpdateTime = 1000; // Interval for foreground updates in milliseconds
    private long foregroundMinDistance = 5; // Minimum distance for foreground updates in meters
    private long backgroundUpdateTime = 10000; // Interval for background updates (5 minutes)
    private long backgroundMinDistance = 50; // Minimum distance for background updates in meters


    private List<double[]> coordinatesArray = new ArrayList<>();

    private static final String CHANNEL_ID = "Tracking";
    private long startTime;
    private long endTime;
    private NotificationCompat.Builder notificationBuilder;
    private DownloadCallback callback;

    private long elapsedTime;
    private NotificationManagerCompat notificationManager;
    private Notification notification;
    private final IBinder binder = new LocalBinder();

    private LocationManager locationManager;
    private static final int NOTIF_ID = 1;

    private locationUpdate locationListener;
    private boolean startTracking = true;

    private double distanceTravelled;

    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {



       startLocationTracking();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Start the notification

        // Pass notification components to locationUpdate
        locationListener = new locationUpdate(this);

//        try {
//
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    1000, // Minimum time interval between updates (in milliseconds)
//                    5, // Minimum distance between updates (in meters)
//                    locationListener);
//        } catch (SecurityException e) {
//            Log.d("comp3018", e.toString());
//        }
        new Thread(() -> {
            startTime = System.currentTimeMillis(); // Capture the start time

            while (startTracking) { // Add a loop to continuously monitor startTracking variable
                try {
                    long currentTime = System.currentTimeMillis();
                    elapsedTime = currentTime - startTime; // Update elapsed time continuously

                    Thread.sleep(1000); // Sleep for a while
                    distanceTravelled = locationListener.getDistanceInKilometers();
                    saveLocationToRouteList(locationListener.getCurrentLatitude(), locationListener.getCurrentLongitude());

                    // Notify the callback about the distance update
                    if (callback != null) {
                        callback.checkProg((int) distanceTravelled); // Notify the callback with the updated distance
                    }


                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        return START_STICKY;
    }



    private void saveLocationToRouteList(double latitude, double longitude) {
        double[] coordinates = {latitude, longitude};
        coordinatesArray.add(coordinates);
    }

    // Method to get the collected coordinates
    public List<double[]> getCoordinatesArray() {

        return coordinatesArray;
    }
    private void startLocationTracking(){
        // ... (other code)
        // Create a notification channel for Android Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Service")
                .setContentText("Tracking in progress")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        // Start the service as a foreground service
        startForeground(NOTIF_ID, notification);



    }






    @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }

    // Method to get the total distance travelled
    public double getDistanceTravelled() {
        return distanceTravelled;
    }



    public String getPace() {
        if (distanceTravelled > 0 && elapsedTime > 0) {
            double paceInMinutesPerKm = elapsedTime / 1000.0 / 60.0 / distanceTravelled;

            // Calculate pace minutes and seconds
            int paceMinutes = (int) paceInMinutesPerKm;
            int paceSeconds = (int) ((paceInMinutesPerKm - paceMinutes) * 60);

            // Format the pace as "mm:ss per km"
            return String.format("Current pace %02d:%02d", paceMinutes, paceSeconds);
        }
        return "00:00 per km"; // Default value if distanceTravelled is zero or elapsedTime is zero
    }

    public String getDuration() {
        // Calculate duration in seconds
        int durationSeconds = (int) (elapsedTime / 1000);

        // Calculate duration in minutes and seconds
        int durationMinutes = durationSeconds / 60;
        durationSeconds = durationSeconds % 60;


        Log.d("bosaioibsdboifbiofrn", "Duration: " + elapsedTime + " " + durationSeconds + "");
        // Format the duration as "mm:ss"
        return String.format("%02d:%02d", durationMinutes, durationSeconds);
    }


    // Method to control the startTracking variable
    public void stopTracking() {
        startTracking = false;
            startTracking = false;
            endTime = System.currentTimeMillis(); // Capture the end time
            // Calculate duration and average pace here
            calculateExerciseStats();

    }

    private void calculateExerciseStats() {
        // Calculate duration in milliseconds
        long durationInMillis = endTime - startTime;

        // Convert duration to seconds
        int durationInSeconds = (int) (durationInMillis / 1000);

        // Calculate duration in minutes and hours
        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;

        // Format the duration as "hh:mm"
        String durationFormatted = String.format("%02d:%02d", hours, minutes);

        // Calculate average pace in seconds per kilometer
        double averagePaceSecondsPerKm = 0.0;
        if (distanceTravelled > 0 && durationInSeconds > 0) {
            double pace = durationInSeconds / distanceTravelled; // Pace in seconds per kilometer
            averagePaceSecondsPerKm = pace;
        }

        // Format the average pace as "mm:ss" (minutes:seconds)
        int paceMinutes = (int) (averagePaceSecondsPerKm / 60);
        int paceSeconds = (int) (averagePaceSecondsPerKm % 60);
        String averagePaceFormatted = String.format("%02d:%02d", paceMinutes, paceSeconds);

        // Log or use the calculated values as needed
        Log.d("ExerciseStats", "Duration: " + durationFormatted);
        Log.d("ExerciseStats", "Average Pace: " + averagePaceFormatted + " per km");
    }


    public void onAppInBackground() {
        Log.d("idonifnoisdfniosnof", "background usage");
        if (locationManager != null && locationListener != null) {
            // Adjust the location update intervals for background state
            try {
                locationManager.removeUpdates(locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        backgroundUpdateTime,
                        backgroundMinDistance,
                        locationListener);
            } catch (SecurityException e) {
                Log.d("comp3018", e.toString());
            }
        }
    }

    public void onAppInForeground() {
        Log.d("idonifnoisdfniosnof", "foreground usage");
        if (locationManager != null && locationListener != null) {
            // Restore regular location update intervals for foreground state
            try {
                locationManager.removeUpdates(locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        foregroundUpdateTime,
                        foregroundMinDistance,
                        locationListener);
            } catch (SecurityException e) {
                Log.d("comp3018", e.toString());
            }
        }
    }


    //SERVICE HANDLERS
    public interface DownloadCallback{
        void checkProg(int prog);
    }
    public void setCallback(DownloadCallback callback){
        this.callback = callback;
    }

    public class LocalBinder extends Binder {
        LocationTrackingService getService() {
            return LocationTrackingService.this;
        }
    }

}