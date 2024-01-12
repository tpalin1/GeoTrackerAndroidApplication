package com.example.gps_locatorcw.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.viewmodel.GeofenceViewModel;
import com.example.gps_locatorcw.databases.DAO.GeofenceDAO;
import com.example.gps_locatorcw.databases.GeofenceDatabase;
import com.example.gps_locatorcw.databases.entities.GeofenceStats;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "GeofenceReceiver";
    private static final String CHANNEL_ID = "GeofenceChannel";
    private static final int NOTIFICATION_ID = 1001;
    public GeofenceViewModel geofenceViewModel;
    private GeofenceDAO geofenceDAO;


    /**
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
     * @param context The Context in which the receiver is running.
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        geofenceViewModel = new GeofenceViewModel();
        // Get the transition type and the geofences triggered
        int transition = geofencingEvent.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.d("GeofenceBroadcast", "onReceive: GEOFENCE_TRANSITION_ENTER");
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

            GeofenceDatabase geofenceDatabase = GeofenceDatabase.getDatabase(context);
            GeofenceDAO geofenceDAO = geofenceDatabase.geofenceDAO();

            LiveData<List<GeofenceStats>> allGeofencesLiveData = geofenceDAO.getAllGeofencesAsync();


            allGeofencesLiveData.observeForever(new Observer<List<GeofenceStats>>() {
                @Override
                public void onChanged(List<GeofenceStats> allGeofences) {


                    for (Geofence triggeredGeofence : triggeredGeofences) {
                        String triggeredGeofenceId = triggeredGeofence.getRequestId();

                        for (GeofenceStats geofenceStats : allGeofences) {

                            //Check if the geofence that was triggered is the same as the one in the database
                            Log.d("GeofenceBroadcast", "onChanged: " + geofenceStats.getGeofenceName());
                            Log.d("GeofenceBroadcast", "onChanged: " + triggeredGeofenceId);

                            if (geofenceStats.getGeofenceName().equals(triggeredGeofenceId) ){

                                //Get the reminder from the database and show a notification
                                String reminder = geofenceStats.getReminder();
                                showNotification(context, "Entered", reminder);
                                //Delete the geofence from the database
                                GeofenceDatabase.databaseWriteExecutor.execute(() -> {
                                    geofenceDAO.delete(geofenceStats);
                                });


                                break;
                            }
                        }
                    }




                    allGeofencesLiveData.removeObserver(this);
                }
            });
        }
    }


    /**
     * @param context The Context in which the receiver is running.
     * @param transitionText
     * @param reminder
     * This method is used to show a notification when a geofence transition is triggered.
     */
    private void showNotification(Context context, String transitionText, String reminder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Geofence Channel";
            String description = "Channel for Geofence Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Geofence Transition: " + transitionText)
                .setContentText(reminder)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = builder.build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}


