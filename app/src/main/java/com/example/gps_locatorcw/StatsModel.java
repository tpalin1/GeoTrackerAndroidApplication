package com.example.gps_locatorcw;

public class StatsModel {

    String name;
    double duration;


    public StatsModel(String name, double duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public double getDuration() {
        return duration;
    }
}
