package com.example.gps_locatorcw;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.gps_locatorcw.ExerciseStats;

import java.util.List;

public class ExerciseStatsViewModel extends ViewModel {

    private ExerciseStatsRepository repository;
    private LiveData<List<ExerciseStats>> allExerciseStats;



    // Empty constructor
    public ExerciseStatsViewModel() {
        // Instantiate your repository here if needed
    }

    public void init(ExerciseStatsRepository repository) {
        this.repository = repository;
        allExerciseStats = repository.getAllExerciseStatsAsync();
    }

    public LiveData<List<ExerciseStats>> getAllExerciseStats() {
        return allExerciseStats;
    }




}
