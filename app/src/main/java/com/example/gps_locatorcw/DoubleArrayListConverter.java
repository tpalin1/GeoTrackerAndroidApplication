package com.example.gps_locatorcw;
import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DoubleArrayListConverter {

    @TypeConverter
    public static String fromDoubleArrayList(List<double[]> list) {
        StringBuilder sb = new StringBuilder();
        for (double[] array : list) {
            for (double value : array) {
                sb.append(value).append(",");
            }
            sb.append(";");
        }
        return sb.toString();
    }

    @TypeConverter
    public static List<double[]> toDoubleArrayList(String value) {
        List<double[]> list = new ArrayList<>();
        String[] arrays = value.split(";");
        for (String array : arrays) {
            String[] values = array.split(",");
            double[] doubleArray = new double[values.length];
            for (int i = 0; i < values.length; i++) {
                doubleArray[i] = Double.parseDouble(values[i]);
            }
            list.add(doubleArray);
        }
        return list;
    }
}
