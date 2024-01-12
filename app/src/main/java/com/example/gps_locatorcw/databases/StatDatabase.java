package com.example.gps_locatorcw.databases;
import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.gps_locatorcw.databases.entities.ExerciseStats;
import com.example.gps_locatorcw.databases.DAO.StatDAO;

@Database(entities = {ExerciseStats.class}, version = 11, exportSchema = false)
public abstract class StatDatabase extends RoomDatabase {

    private static final int threadCount = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(threadCount);

    public static StatDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (StatDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    StatDatabase.class, "user_stattable")
                            .fallbackToDestructiveMigration()
                            .addCallback(createCallback)

                            .build();
                }
            }
        }
        return instance;
    }


    public abstract StatDAO statDAO();

    private static volatile StatDatabase instance;
    public static StatDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (StatDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    StatDatabase.class, "user_stattable")
                            .fallbackToDestructiveMigration()
.addCallback(createCallback)

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

            databaseWriteExecutor.execute(() -> {

            });
        }
    };





}