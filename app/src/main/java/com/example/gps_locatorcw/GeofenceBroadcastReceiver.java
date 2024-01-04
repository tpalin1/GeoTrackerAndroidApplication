package com.example.gps_locatorcw;

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

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "GeofenceReceiver";
    private static final String CHANNEL_ID = "GeofenceChannel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

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
                    // Iterate through all geofences in the database to find a match
                    for (Geofence triggeredGeofence : triggeredGeofences) {
                        String triggeredGeofenceId = triggeredGeofence.getRequestId();

                        for (GeofenceStats geofenceStats : allGeofences) {
                            Log.d("GeofenceBroadcast", "onChanged: " + geofenceStats.getGeofenceName());
                            Log.d("GeofenceBroadcast", "onChanged: " + triggeredGeofenceId);

                            if (geofenceStats.getGeofenceName().equals(triggeredGeofenceId) ){

                                // Match found for the triggered geofence in the database
                                String reminder = geofenceStats.getReminder();
                                showNotification(context, "Entered", reminder);
                                break; // Stop further iteration once a match is found
                            }
                        }
                    }

                    // Don't forget to remove the observer when it's no longer needed
                    allGeofencesLiveData.removeObserver(this);
                }
            });
        }
    }


    // Method to extract numeric part from a string
    // Method to extract numeric part from a string
    private int extractNumericPart(String str) {
        StringBuilder numericPart = new StringBuilder();
        boolean foundNumeric = false;

        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                numericPart.append(c);
                foundNumeric = true;
            } else if (foundNumeric && c == '.') {
                // If a decimal point is found after the numeric part, break the loop
                break;
            }
        }

        if (numericPart.length() == 0) {
            // Handle the case where no numeric part was found
            return -1; // or throw an exception, return a default value, etc.
        }

        // Parse the extracted numeric part to integer
        return Integer.parseInt(numericPart.toString());
    }
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


