package com.example.gps_locatorcw.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class PolylineViewModel  extends ViewModel {
    private MutableLiveData<List<LatLng>> polylinePoints = new MutableLiveData<>();

    public LiveData<List<LatLng>> getPolylinePoints() {
        return polylinePoints;
    }

    public void updatePolyline(List<LatLng> points) {
        List<LatLng> copiedList = new ArrayList<>(points);
        polylinePoints.setValue(copiedList);
    }
}
