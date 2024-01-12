package com.example.gps_locatorcw.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;
import com.example.gps_locatorcw.repos.ExerciseStatsRepository;

import java.util.List;

public class ExerciseStatsViewModel extends ViewModel {

    private ExerciseStatsRepository repository;
    private LiveData<List<ExerciseStats>> allExerciseStats;




    public ExerciseStatsViewModel() {

    }

    public void init(ExerciseStatsRepository repository) {
        this.repository = repository;
        allExerciseStats = repository.getAllExerciseStatsAsync();
    }

    public LiveData<List<ExerciseStats>> getAllExerciseStats() {
        return allExerciseStats;
    }




}
