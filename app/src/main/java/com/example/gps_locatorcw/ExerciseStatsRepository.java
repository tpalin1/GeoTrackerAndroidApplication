package com.example.gps_locatorcw;

import androidx.lifecycle.LiveData;
import java.util.List;

// ExerciseStatsRepository.java
public class ExerciseStatsRepository {

    private StatDAO statDAO;

    public ExerciseStatsRepository(StatDAO statDAO) {
        this.statDAO = statDAO;
    }


    // Add a suspend function to fetch data asynchronously
    public LiveData<List<ExerciseStats>> getAllExerciseStatsAsync() {
        return statDAO.getAllExerciseStatsAsync();
    }
}
