package com.example.gps_locatorcw.Fragments;
import static com.example.gps_locatorcw.utils.StatRecycler.exerciseStats;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.gps_locatorcw.viewmodel.ExerciseStatsViewModel;
import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.databases.DAO.StatDAO;
import com.example.gps_locatorcw.databases.StatDatabase;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;
import com.example.gps_locatorcw.repos.ExerciseStatsRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class DisplayDataFragment extends Fragment {

    private StatDAO statDAO;
    private BarChart barChart;
    StatDatabase database;

    private ExerciseStatsViewModel exerciseStatsViewModel;

    /**
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return
     *
     * Used for monitoring the distance thbe user takes in each exercise
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_data, container, false);

        barChart = view.findViewById(R.id.barChart);
        database = StatDatabase.getDatabase(requireContext());
        statDAO = database.statDAO();

        exerciseStatsViewModel = new ViewModelProvider(this).get(ExerciseStatsViewModel.class);
        exerciseStatsViewModel.init(new ExerciseStatsRepository(statDAO));

        exerciseStatsViewModel.getAllExerciseStats().observe(getViewLifecycleOwner(), exerciseStats -> {
            List<BarEntry> entries = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            // Iterate through exerciseStats and populate entries and labels
            for (int i = 0; i < exerciseStats.size(); i++) {
                ExerciseStats stats = exerciseStats.get(i);
                entries.add(new BarEntry(i, (float) stats.getDuration()));
                labels.add(stats.getExercise());
            }

            BarDataSet dataSet = new BarDataSet(entries, "Exercise Durations");
            BarData barData = new BarData(dataSet);

            for (int i = 0; i < exerciseStats.size(); i++) {
                ExerciseStats stats = exerciseStats.get(i);
                entries.add(new BarEntry(i, (float) stats.getDuration()));
                labels.add(stats.getExercise());
            }

            dataSet.setValueTextColor(Color.WHITE);

            XAxis xAxis = barChart.getXAxis();
            xAxis.setTextColor(Color.WHITE);
            xAxis.setTextSize(14f); // Set the text size for x-axis labels
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);  // Set position to bottom
            xAxis.setGranularity(1f);  // Ensure only one label per exercise

            YAxis yAxisLeft = barChart.getAxisLeft();
            YAxis yAxisRight = barChart.getAxisRight();

            yAxisLeft.setTextColor(Color.WHITE);
            yAxisRight.setTextColor(Color.WHITE);

            barChart.setDescription(null);  // Remove description label

            barChart.setData(barData);
            barChart.invalidate();
        });





        return view;
    }

}
