package com.example.gps_locatorcw;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class StatsViewModel extends ViewModel {
    private MutableLiveData<Double> distanceTravelled;

    private MutableLiveData<String> exerciseDuration; // Store the exercise duration
    private MutableLiveData<String> averagePace; // Store the average pace





    public StatsViewModel() {
        distanceTravelled = new MutableLiveData<>();
        distanceTravelled.setValue(0.0);

        exerciseDuration = new MutableLiveData<>();
        averagePace = new MutableLiveData<>();

        // Set initial values for duration and pace (if needed)
        exerciseDuration.setValue("00:00"); // Set initial duration as 00:00
        averagePace.setValue("00:00"); // Set initial pace as 00:00 per km

    }

    public LiveData<Double> getDistanceTravelled() {
        return distanceTravelled;
    }

    public void updateDistance(double distance) {
        double currentDistance = distanceTravelled.getValue();
        distanceTravelled.setValue(distance);
    }

    // Methods to retrieve LiveData for exercise duration and average pace
    public LiveData<String> getExerciseDuration() {
        return exerciseDuration;
    }

    public LiveData<String> getAveragePace() {
        return averagePace;
    }

    // Methods to update exercise duration and average pace
    public void updateExerciseDuration(String duration) {
        exerciseDuration.setValue(duration);
    }

    public void updateAveragePace(String pace) {
        averagePace.setValue(pace);
    }
}