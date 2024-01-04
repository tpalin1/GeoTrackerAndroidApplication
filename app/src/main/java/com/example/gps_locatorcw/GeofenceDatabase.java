package com.example.gps_locatorcw;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {GeofenceStats.class}, version = 2, exportSchema = false)

public abstract class GeofenceDatabase extends RoomDatabase {

    private static final int threadCount = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(threadCount);
    public abstract GeofenceDAO geofenceDAO();

    private static volatile GeofenceDatabase instance;
    static GeofenceDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (GeofenceDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    GeofenceDatabase.class, "geofence_table")
                            .fallbackToDestructiveMigration()
                            .addCallback(createCallback)
//#allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }

    private static final RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Perform database operations here upon creation
            databaseWriteExecutor.execute(() -> {
                // Do something upon database creation if needed
            });
        }
    };



}
