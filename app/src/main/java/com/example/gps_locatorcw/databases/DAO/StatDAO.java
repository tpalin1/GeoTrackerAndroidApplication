package com.example.gps_locatorcw.databases.DAO;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gps_locatorcw.databases.entities.ExerciseStats;

import java.util.List;
@Dao
public interface StatDAO {





    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ExerciseStats stat);

    @Query("SELECT * FROM user_stattable")
    Cursor getAllExerciseStatsCursor();


    @Query("UPDATE user_stattable SET exercise = :newExerciseName WHERE exercise = :oldExerciseName")
    void updateExerciseName(String oldExerciseName, String newExerciseName);

    @Query("SELECT * FROM user_stattable")
    List<ExerciseStats> getAllExerciseStats();

    @Update
    void updateExerciseStats(List<ExerciseStats> exerciseStats);




    @Query("SELECT * FROM user_stattable")
    List<ExerciseStats> getAllCats();

    @Query("SELECT * FROM user_stattable ORDER BY exercise ASC")
    List<ExerciseStats> getAlphabetizedCats();

    @Query("SELECT * FROM user_stattable WHERE exerciseType = 'Run'")
    List<ExerciseStats> getRun();

    @Query("SELECT * FROM user_stattable WHERE exerciseType = 'Walk'")
    List<ExerciseStats> getWalk();

    @Query("SELECT * FROM user_stattable")
    LiveData<List<ExerciseStats>> getAllExerciseStatsAsync();

    @Query("SELECT * FROM user_stattable WHERE exercise = :exercise")
    ExerciseStats getExerciseByName(String exercise);
}