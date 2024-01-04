package com.example.gps_locatorcw;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
@Database(entities = {ExerciseStats.class}, version = 5, exportSchema = false)
public abstract class StatDatabase extends RoomDatabase {

    private static final int threadCount = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(threadCount);
    public abstract StatDAO statDAO();

    private static volatile StatDatabase instance;
    static StatDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (StatDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    StatDatabase.class, "user_stattable")
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