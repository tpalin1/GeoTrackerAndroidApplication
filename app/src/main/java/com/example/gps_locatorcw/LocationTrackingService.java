package com.example.gps_locatorcw;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocationTrackingService extends Service {
    public LocationTrackingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}