package com.example.gps_locatorcw.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.gps_locatorcw.services.LocationTrackingService;

public class NotificationButtonClick extends BroadcastReceiver {
    /**
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     * For when the user clicks pause in the notification app
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null && action.equals("PAUSE_TRACKING")) {

            LocationTrackingService.toggleTracking(context);
        }

        if (action != null && action.equals("STOP_TRACKING")) {


        }
    }
}
