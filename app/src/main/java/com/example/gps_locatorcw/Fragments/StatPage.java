package com.example.gps_locatorcw.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gps_locatorcw.Fragments.DisplayDataFragment;
import com.example.gps_locatorcw.Fragments.DisplayExercise;
import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.interfaces.RouteClick;
import com.example.gps_locatorcw.utils.StatRecycler;
import com.example.gps_locatorcw.databases.DAO.StatDAO;
import com.example.gps_locatorcw.databases.StatDatabase;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatPage extends Fragment implements RouteClick {
    private StatDatabase database;
    private StatDAO statDAO;
    private List<ExerciseStats> exerciseStatsList = new ArrayList<>();
    private List<ExerciseStats> originalExerciseStatsList = new ArrayList<>();
    private StatRecycler statRecycler;
    private RecyclerView recyclerView;
    private Spinner sortingOptionsSpinner;

    public StatPage() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_stat_page, container, false);

        recyclerView = view.findViewById(R.id.statsRecycler);
        database = StatDatabase.getDatabase(requireContext());
        statDAO = database.statDAO();

        Button showDataButton = view.findViewById(R.id.check_data);
        showDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayDataFragment();
            }
        });




        sortingOptionsSpinner = view.findViewById(R.id.sortingOptionsSpinner);
        String[] sortingOptions = {"Recent", "Most Duration", "Run", "Walk", "Cycle"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortingOptions);
        sortingOptionsSpinner.setAdapter(spinnerAdapter);
        statRecycler = new StatRecycler(requireContext(), exerciseStatsList, this);
        recyclerView.setAdapter(statRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        sortingOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        fetchMostRecentData();
                        break;
                    case 1:
                        sortByMostDuration();
                        break;
                    case 2:
                        fetchAndDisplayRunExercises();
                        break;
                    case 3:
                        fetchAndDisplayWalkExercises();
                        break;
                    case 4:
                        fetchAndDisplayCycleExercises();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    private void sortByMostDuration() {
        StatDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseStats> allExerciseStats = statDAO.getAllExerciseStats();
            Collections.sort(allExerciseStats, new Comparator<ExerciseStats>() {
                @Override
                public int compare(ExerciseStats o1, ExerciseStats o2) {
                    return Double.compare(o2.getDuration(), o1.getDuration());
                }
            });

            requireActivity().runOnUiThread(() -> {
                statRecycler.setData(allExerciseStats);
            });
        });
    }

    private void displayDataFragment() {
        DisplayDataFragment dataFragment = new DisplayDataFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_fragment_container, dataFragment)
                .addToBackStack(null)
                .commit();

        FrameLayout mapFragmentContainer = requireActivity().findViewById(R.id.map_fragment_container);
        mapFragmentContainer.bringToFront();
    }

    private void fetchAndDisplayRunExercises() {
        StatDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseStats> runExercises = statDAO.getRun();
            requireActivity().runOnUiThread(() -> {
                statRecycler.setData(runExercises);
            });
        });
    }

    private void fetchAndDisplayCycleExercises() {
        StatDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseStats> cycleExercises = statDAO.getCycle();
            requireActivity().runOnUiThread(() -> statRecycler.setData(cycleExercises));
        });
    }



    private void fetchAndDisplayWalkExercises() {
        StatDatabase.databaseWriteExecutor.execute(() -> {
            List<ExerciseStats> walkExercises = statDAO.getWalk();
            requireActivity().runOnUiThread(() -> statRecycler.setData(walkExercises));
        });
    }


    private void fetchMostRecentData() {
        StatDatabase.databaseWriteExecutor.execute(() -> {
            exerciseStatsList = statDAO.getAllExerciseStats();
            Collections.reverse(exerciseStatsList);
            requireActivity().runOnUiThread(() -> {
                statRecycler.setData(exerciseStatsList);
            });
        });
    }

    @Override
    public void onRouteClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            ExerciseStats clickedExercise = statRecycler.filteredStats.get(position);
            int originalPosition = exerciseStatsList.indexOf(clickedExercise);

            Log.d("ExerciseStats", "Exercise: " + clickedExercise.getExercise() +
                    ", Duration: " + clickedExercise.getDuration());

            if (clickedExercise != null) {
                StatDatabase.databaseWriteExecutor.execute(() -> {
                    ExerciseStats exerciseDetails = statDAO.getExerciseByName(clickedExercise.getExercise());

                    requireActivity().runOnUiThread(() -> {
                        DisplayExercise displayExerciseFragment = new DisplayExercise();

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("coordinates", new ArrayList<>(exerciseDetails.getCoordinates()));
                        bundle.putString("exerciseName", exerciseDetails.getExercise());
                        displayExerciseFragment.setArguments(bundle);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.map_fragment_container, displayExerciseFragment)
                                .addToBackStack(null)
                                .commit();

                        FrameLayout mapFragmentContainer = requireActivity().findViewById(R.id.map_fragment_container);
                        mapFragmentContainer.bringToFront();
                    });
                });
            }
        }
    }

    @Override
    public void onEditClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            ExerciseStats clickedExercise = statRecycler.filteredStats.get(position);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Edit notes");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newExerciseName = input.getText().toString();

                    if (!TextUtils.isEmpty(newExerciseName)) {
                        StatDatabase.databaseWriteExecutor.execute(() -> {
                            String oldExerciseName = clickedExercise.getExercise();
                            statDAO.updateExerciseName(oldExerciseName, newExerciseName);

                            clickedExercise.setExercise(newExerciseName);

                            requireActivity().runOnUiThread(() -> {
                                statRecycler.notifyItemChanged(position);
                            });
                        });
                    } else {
                        Toast.makeText(requireContext(), "Please enter a valid exercise name", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }
    }
}
