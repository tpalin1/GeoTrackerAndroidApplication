package com.example.gps_locatorcw.repos;

import androidx.lifecycle.LiveData;

import com.example.gps_locatorcw.databases.DAO.StatDAO;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;

import java.util.List;


public class ExerciseStatsRepository {

    private StatDAO statDAO;

    public ExerciseStatsRepository(StatDAO statDAO) {
        this.statDAO = statDAO;
    }



    public LiveData<List<ExerciseStats>> getAllExerciseStatsAsync() {
        return statDAO.getAllExerciseStatsAsync();
    }
}
