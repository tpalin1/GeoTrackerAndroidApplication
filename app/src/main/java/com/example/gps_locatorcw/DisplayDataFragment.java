package com.example.gps_locatorcw;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;
import java.util.List;

public class DisplayDataFragment extends Fragment {

    private StatDAO statDAO; // Your StatDAO instance
    private BarChart barChart;
    StatDatabase database;

    private ExerciseStatsViewModel exerciseStatsViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_display_data, container, false);

        barChart = view.findViewById(R.id.barChart);
        database = StatDatabase.getDatabase(requireContext());
        statDAO = database.statDAO();

        exerciseStatsViewModel = new ViewModelProvider(this).get(ExerciseStatsViewModel.class);
        exerciseStatsViewModel.init(new ExerciseStatsRepository(statDAO));


            exerciseStatsViewModel.getAllExerciseStats().observe(getViewLifecycleOwner(), exerciseStats -> {
                // Update UI with the retrieved exercise stats here
                List<BarEntry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                for (int i = 0; i < exerciseStats.size(); i++) {
                    ExerciseStats stats = exerciseStats.get(i);
                    entries.add(new BarEntry(i, (float) stats.getDuration()));
                    labels.add(stats.getExercise());
                }

                BarDataSet dataSet = new BarDataSet(entries, "Exercise Durations");
                BarData barData = new BarData(dataSet);

                // Set value text color to white
                dataSet.setValueTextColor(Color.WHITE);

                // Customize X-axis labels color
                XAxis xAxis = barChart.getXAxis();
                xAxis.setTextColor(Color.WHITE);

                // Customize Y-axis labels color
                barChart.getAxisLeft().setTextColor(Color.WHITE);
                barChart.getAxisRight().setTextColor(Color.WHITE);

                barChart.setData(barData);
                barChart.invalidate(); // Refresh chart
            });


        return view;
    }
}
