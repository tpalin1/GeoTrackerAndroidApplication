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
            for (int i = 0; i < exerciseStats.size(); i++) {
                ExerciseStats stats = exerciseStats.get(i);
                entries.add(new BarEntry(i, (float) stats.getDuration()));
                labels.add(stats.getExercise());
            }

            BarDataSet dataSet = new BarDataSet(entries, "Exercise Durations");
            BarData barData = new BarData(dataSet);


            dataSet.setValueTextColor(Color.WHITE);


            XAxis xAxis = barChart.getXAxis();
            xAxis.setTextColor(Color.WHITE);


            barChart.getAxisLeft().setTextColor(Color.WHITE);
            barChart.getAxisRight().setTextColor(Color.WHITE);

            barChart.setData(barData);
            barChart.invalidate();

        });

        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            /**
             * @param e The selected Entry
             * @param h The corresponding highlight object that contains information
             *          about the highlighted position such as dataSetIndex
             *          or the selected x-value.
             *          Called when a value has been selected inside the chart.
             *
             */
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int selectedIndex = (int) e.getX();
                if (selectedIndex >= 0 && selectedIndex < exerciseStats.size()) {
                    ExerciseStats selectedExercise = exerciseStats.get(selectedIndex);

                    displayExerciseDetails(selectedExercise);
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });




        return view;
    }

    /**
     * @param exercise The exercise to display details for the duration
     *                 Method to display exercise details in a custom popup dialog
     */

    private void displayExerciseDetails(ExerciseStats exercise) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Exercise Details");


        String message = "Exercise: " + exercise.getExercise() + "\nDuration: " + exercise.getDuration();
        builder.setMessage(message);


        builder.setPositiveButton("OK", (dialog, which) -> {

            dialog.dismiss();
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
