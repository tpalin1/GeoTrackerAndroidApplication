package com.example.gps_locatorcw;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface StatDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ExerciseStats stat);



    @Query("UPDATE user_stattable SET exercise = :newExerciseName WHERE exercise = :oldExerciseName")
    void updateExerciseName(String oldExerciseName, String newExerciseName);

    @Query("SELECT * FROM user_stattable")
    List<ExerciseStats> getAllExerciseStats();

    @Update
    void updateExerciseStats(ExerciseStats exerciseStats);

    @Query("SELECT * FROM user_stattable")
    List<ExerciseStats> getAllCats();

    @Query("SELECT * FROM user_stattable ORDER BY exercise ASC")
    List<ExerciseStats> getAlphabetizedCats();

    @Query("SELECT * FROM user_stattable WHERE exerciseType = 'Run'")
    List<ExerciseStats> getRun();

    @Query("SELECT * FROM user_stattable WHERE exerciseType = 'Walk'")
    List<ExerciseStats> getWalk();

    @Query("SELECT * FROM user_stattable")
    LiveData<List<ExerciseStats>> getAllExerciseStatsAsync(); // Return LiveData with coroutines

}