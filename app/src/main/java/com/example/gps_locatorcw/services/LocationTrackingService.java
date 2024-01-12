package com.example.gps_locatorcw.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gps_locatorcw.activities.MainActivity;
import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.utils.NotificationButtonClick;
import com.example.gps_locatorcw.utils.locationUpdate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Foregournd service for the users location
 */
public class LocationTrackingService extends Service {


    public LocationTrackingService() {
    }

    private long foregroundUpdateTime = 1000;
    private long foregroundMinDistance = 5;
    private long backgroundUpdateTime = 10000;
    private long backgroundMinDistance = 50;


    private List<double[]> coordinatesArray = new ArrayList<>();

    private RemoteViews remoteViewsLarge;
    private RemoteViews remoteViewsSmall;

    private static boolean isPaused = false;

    private boolean hasStarted = false;

    private static final String CHANNEL_ID = "Tracking";
    private long startTime;
    private long endTime;

    private List<LatLng> locationList = new ArrayList<>();



    private NotificationCompat.Builder notificationBuilder;
    private DownloadCallback callback;

    private PolylineOptions polylineOptions;
    private Polyline polyline;

    private long elapsedTime;
    private NotificationManagerCompat notificationManager;
    private Notification notification;
    private final IBinder binder = new LocalBinder();

    private boolean hasActivity = false;
    private LocationManager locationManager;
    private static final int NOTIF_ID = 1;

    private locationUpdate locationListener;

    PendingIntent contentPendingIntent;

    private static final int NOTIF_ID_START = 2;

    private boolean startTracking = false;

    private double distanceTravelled;

    private boolean isTrackingPaused = false;


    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        hasActivity = true;


        //dont go into it
        if(hasStarted()){
            return START_NOT_STICKY;
        }
        //Create the tracking service notification
        createNotif();



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        locationListener = new locationUpdate(this);

        new Thread(() -> {
            startTime = System.currentTimeMillis();
            while (true) {
                try {
                    if(!isPaused && startTracking && !isTrackingPaused){
                        long currentTime = System.currentTimeMillis();
                        //Get the elapsed tinem
                        elapsedTime = currentTime - startTime;
                        Thread.sleep(1000);
                        distanceTravelled = locationListener.getDistanceInKilometers();


                        Log.d("ADBSIOHD", "Distance: " + distanceTravelled + " km");
                        //Save all the locations to the list and then draw the polyline later
                        saveLocationToRouteList(locationListener.getCurrentLatitude(), locationListener.getCurrentLongitude());
                        //Update the notification text
                        if (callback != null) {
                            remoteViewsLarge.setTextViewText(R.id.notificationDuration, getDuration());
                            remoteViewsLarge.setTextViewText(R.id.notificationTimer, getDuration());
                            remoteViewsLarge.setTextViewText(R.id.notificationDistance, String.format("%.2f km", distanceTravelled));
                            Log.d("sioafhidsa", "acsnpodfnpof");
                            callback.checkProg((int) distanceTravelled);

                            // Update the notification text
                            updateNotificationText(getDuration(), getDuration(), String.format("%.2f km", distanceTravelled));
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        return START_STICKY;
    }


    /**
     * Method to create the notification
     * Called when the service is started and makes the notification display that the sevrice is tracking the user.
     *
     */
    public void createNotif() {

        Intent contentIntent = new Intent(this, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this,
                1000,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        // Create the notification that shows that the foreground is tracking
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tracking In Progress")
                .setContentText("Location being tracked")
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // NotificationManagerCompat.from(this).notify(NOTIF_ID, builder.build());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Update: Use NotificationChannel if SDK version is Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ID);

        }

        startForeground(NOTIF_ID, builder.build());
    }


    public boolean isPaused() {
        return isPaused;
    }
    private void updateNotificationText(String duration, String timer, String distance) {
        // Cancel the existing notification
        remoteViewsLarge.setTextViewText(R.id.notificationDuration, duration);
        remoteViewsLarge.setTextViewText(R.id.notificationTimer, timer);
        remoteViewsLarge.setTextViewText(R.id.notificationDistance, distance);

        // Update the custom notification
        Notification customNotificationLarge = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsLarge)
                .setContentIntent(contentPendingIntent)
                .setOnlyAlertOnce(true)
                .build();

        // Notify the NotificationManager about the update
        notificationManager.notify(NOTIF_ID, customNotificationLarge);

    }

    public List<LatLng> getLocationList() {
        return locationList;
    }


    /**
     * @param context Context of the application
     *                Pause and resume the tracking service
     */
    public static void toggleTracking(Context context) {
        isPaused = !isPaused;
    }


    /**
     * @param latitude longitude latitude and longitude of the user's current location
     * * This method is used to update the polyline with the user's location change.
     * It also removes the previous polyline and draws the updated polyline on the map.
     *                 Method to update the polyline with the user's location change
     *                 It ignores the polylines on the 0.0, and instead focusses on the polylines with coordinates where the user is
     */
    private void saveLocationToRouteList(double latitude, double longitude) {
        double[] coordinates = {latitude, longitude};
        coordinatesArray.add(coordinates);

        if (locationList != null && isStartTracking()) {

            if (latitude != 0.0 || longitude != 0.0) {
                LatLng latLng = new LatLng(latitude,longitude);
                locationList.add(latLng);


            }
        }
    }





    public boolean hasStarted(){
        return hasStarted;
    }
    public boolean isStartTracking() {
        return startTracking;
    }

    public void setStartTracking(boolean startTracking) {
        this.startTracking = startTracking;
    }


    public List<double[]> getCoordinatesArray() {

        return coordinatesArray;
    }

    /**
     * Method to start the location tracking service
     * Called when the user starts the exercise
     * It creates the notification channel for the service that has the exercise
     */
    //Create the notification channel for the service that has the exercise tunning
    private void startLocationTracking() {
        startTracking = true;
        hasStarted = true;
        isPaused = false;
        locationListener.resetDistance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Tracking Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            remoteViewsLarge = new RemoteViews(getPackageName(), R.layout.large_notification);
            remoteViewsSmall = new RemoteViews(getPackageName(), R.layout.custom_notification_button);

            remoteViewsLarge.setTextViewText(R.id.notificationDuration, getDuration());
            remoteViewsLarge.setTextViewText(R.id.notificationTimer, getPace());


            remoteViewsLarge.setTextViewText(R.id.notificationDistance, String.format("%.2f km", distanceTravelled));

            Intent contentIntent = new Intent(this, MainActivity.class);
            contentIntent.putExtra("hasOpenedUpMap", true);



            contentIntent.putExtra("loadMapFragment", true);



            contentIntent.putExtra("displayDialogButton", true);
            contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Intent pauseIntent = new Intent(this, NotificationButtonClick.class);
            pauseIntent.setAction("PAUSE_TRACKING");
            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            remoteViewsLarge.setOnClickPendingIntent(R.id.pauseButton, pausePendingIntent);

             PendingIntent contentPendingIntent = PendingIntent.getActivity(
                    this,
                    1001,
                    contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
            );

            Notification customNotificationLarge = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViewsLarge)
                    .setContentIntent(contentPendingIntent)
                    .setOnlyAlertOnce(true)
                    .build();
            remoteViewsLarge.setOnClickPendingIntent(R.id.stopButton, contentPendingIntent);



            startForeground(NOTIF_ID, customNotificationLarge);

        }





}


    /**
     * Stop the exercise and reset everything
     */
    public void stopExercise() {
        Log.d("comp3018", "Exercise Stats:" + "Distance: " + distanceTravelled + " km" + " Duration: " + getDuration() + " Pace: " + getPace());

        // Stop the foreground service

        // Stop tracking and calculate stats
        stopTracking();
        calculateExerciseStats();

        stopForeground(true);


        // Create the initial notification
        createNotif();

        // Stop the service
        stopSelf();
    }

    /**
     * Reset all ready for the next exercise
     * Called when the user starts a new exercise
     */
    private void resetExerciseData() {
        coordinatesArray.clear();
        locationList.clear();
        distanceTravelled = 0;

        elapsedTime = 0;
        startTime = System.currentTimeMillis();
        endTime = 0;
    }
    public void startExercise(){
        if(!hasStarted){
            stopForeground(true);
            resetExerciseData();
            startLocationTracking();
        }

    }

    @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return binder;
        }


    public double getDistanceTravelled() {
        return distanceTravelled;
    }



    public String getPace() {
        if (distanceTravelled > 0 && elapsedTime > 0) {
            double paceInMinutesPerKm = elapsedTime / 1000.0 / 60.0 / distanceTravelled;
            //Calculate avg oace

            int paceMinutes = (int) paceInMinutesPerKm;
            int paceSeconds = (int) ((paceInMinutesPerKm - paceMinutes) * 60);


            return String.format("Current pace %02d:%02d", paceMinutes, paceSeconds);
        }
        return "00:00 per km";
    }

    public String getDuration() {

        int durationSeconds = (int) (elapsedTime / 1000);


        int durationMinutes = durationSeconds / 60;
        durationSeconds = durationSeconds % 60;



        return String.format("%02d:%02d", durationMinutes, durationSeconds);
    }



    public void stopTracking() {
            startTracking = false;
            hasStarted = false;
            endTime = System.currentTimeMillis();


            calculateExerciseStats();

    }

    private void calculateExerciseStats() {

        long durationInMillis = endTime - startTime;


        int durationInSeconds = (int) (durationInMillis / 1000);


        int hours = durationInSeconds / 3600;
        int minutes = (durationInSeconds % 3600) / 60;


        String durationFormatted = String.format("%02d:%02d", hours, minutes);

        // Calculate the average pace
        double averagePaceSecondsPerKm = 0.0;
        if (distanceTravelled > 0 && durationInSeconds > 0) {
            double pace = durationInSeconds / distanceTravelled;
            averagePaceSecondsPerKm = pace;
        }

        // Convert the average pace to minutes and seconds
        int paceMinutes = (int) (averagePaceSecondsPerKm / 60);
        int paceSeconds = (int) (averagePaceSecondsPerKm % 60);
        String averagePaceFormatted = String.format("%02d:%02d", paceMinutes, paceSeconds);


        //PRINT OUT THE STATS
        Log.d("comp3018", "Exercise Stats:"+ "Distance: " + distanceTravelled + " km" + " Duration: " + durationFormatted + " Pace: " + averagePaceFormatted);


    }


    /**
     * Method to adjust the location update intervals for background state
     * Called when the app is in the background
     * To optimise battery usage, the location update intervals are adjusted
     */
    public void onAppInBackground() {
        if (locationManager != null && locationListener != null && startTracking == true) {
            //Have lower intervals for better batteru usahe

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

    /**
     * Method to adjust the location update intervals for foreground state
     * Called when the app is in the foreground
     * To optimise battery usage, the location update intervals are adjusted
     */
    public void onAppInForeground() {
        if (locationManager != null && locationListener != null ) {
                try {
                    //Higher intervals for better accuracy when in foreground
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



    public interface DownloadCallback{
        void checkProg(int prog);
    }
    public void setCallback(DownloadCallback callback){
        this.callback = callback;
    }

    public class LocalBinder extends Binder {
        public LocationTrackingService getService() {
            return LocationTrackingService.this;
        }
    }

}