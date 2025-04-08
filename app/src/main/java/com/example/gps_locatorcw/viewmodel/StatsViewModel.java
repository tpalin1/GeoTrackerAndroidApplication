package com.example.gps_locatorcw.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatsViewModel extends ViewModel {
    private MutableLiveData<Double> distanceTravelled;

    private MutableLiveData<String> exerciseDuration;
    private MutableLiveData<String> averagePace;

    private MutableLiveData<Boolean> isExercising = new MutableLiveData<>();




    /**
     * Constructor for the ViewModel
     * Initialise the LiveData objects
     * Set initial values for duration and pace (if needed)
     * Set initial value for distance travelled
     */
    public StatsViewModel() {
        distanceTravelled = new MutableLiveData<>();
        distanceTravelled.setValue(0.0);

        exerciseDuration = new MutableLiveData<>();
        averagePace = new MutableLiveData<>();


        exerciseDuration.setValue("00:00");
        averagePace.setValue("00:00");

    }

    /**
     * @return The LiveData object for distance travelled
     */
    public LiveData<Double> getDistanceTravelled() {
        return distanceTravelled;
    }

    /**
     * @param distance The distance to update the LiveData object with
     *                 Method to update the distance travelled
     */
    public void updateDistance(double distance) {
        double currentDistance = distanceTravelled.getValue();
        distanceTravelled.setValue(distance);
    }

    public MutableLiveData<Boolean> getIsExercising() {
        if (isExercising == null) {
            isExercising = new MutableLiveData<>();
        }
        return isExercising;
    }

    public void setIsExercising(boolean exercising) {
        if (isExercising != null) {
            isExercising.setValue(exercising);
        }
    }

    /**
     * @return The LiveData objects for exercise duration and average pace
     */
    // Methods to retrieve LiveData for exercise duration and average pace
    public LiveData<String> getExerciseDuration() {
        return exerciseDuration;
    }

    /**
     * @return The LiveData objects for exercise duration and average pace
     */
    public LiveData<String> getAveragePace() {
        return averagePace;
    }

    /**
     * @param duration The duration to update the LiveData object with
     */
    // Methods to update exercise duration and average pace
    public void updateExerciseDuration(String duration) {
        exerciseDuration.setValue(duration);
    }

    /**
     * @param pace The pace to update the LiveData object with
     */
    public void updateAveragePace(String pace) {
        averagePace.setValue(pace);
    }
}