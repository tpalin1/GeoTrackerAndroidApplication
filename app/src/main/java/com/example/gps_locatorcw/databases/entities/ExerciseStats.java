package com.example.gps_locatorcw.databases.entities;


import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.gps_locatorcw.DoubleArrayListConverter;

import java.util.List;

@Entity(tableName ="user_stattable")
@TypeConverters(DoubleArrayListConverter.class)
public class ExerciseStats {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "exercise")
    private String exercise;


    @ColumnInfo(name = "exerciseType")
    private String exerciseType;
    @ColumnInfo(name = "duration")
    private double duration;
    @ColumnInfo(name = "avgpace")
    private String avgpace;
    @ColumnInfo(name = "distance")
    private String distance;







    @ColumnInfo(name = "coordinates")
    private List<double[]> coordinates;

    public ExerciseStats(@NonNull String exercise, String exerciseType, double duration, List<double[]> coordinates, String distance, String avgpace){
        this.exercise = exercise;
        this.exerciseType = exerciseType;
        this.duration = duration;
        this.coordinates = coordinates;
        this.distance = distance;
        this.avgpace = avgpace;
    }

    public String getExercise() {
        return exercise;
    }

    public String getExerciseType(){
        return exerciseType;
    }

    public void setExerciseType(String exerciseType){
        this.exerciseType = exerciseType;
    }


    public double getDuration(){
        return duration;
    }
    public String getAvgpace() {
        return avgpace;
    }

    public String getDistance(){
        return distance;
    }







    public List<double[]> getCoordinates() {
        return coordinates;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }


}
