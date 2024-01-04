package com.example.gps_locatorcw;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;



public class StatPage extends AppCompatActivity implements RouteClick {
    StatDatabase database;
    StatDAO statDAO;


     List<ExerciseStats> exerciseStatsList = new ArrayList<>();

    StatRecycler statRecycler; // Declare the StatRecycler variable

    RecyclerView recyclerView;


    private RecyclerView statsRecycler;
    private Spinner sortingOptionsSpinner;

    ArrayList<ExerciseStats> statList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_page);

        recyclerView = findViewById(R.id.statsRecycler); // Replace with your RecyclerView ID

        database = StatDatabase.getDatabase(getApplicationContext());
        statDAO = database.statDAO();

        SetUpModel(); // Update UI or perform necessary operations

        Button showDataButton = findViewById(R.id.check_data);
        showDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the data fragment when the button is clicked
                displayDataFragment();
            }
        });





        sortingOptionsSpinner = findViewById(R.id.sortingOptionsSpinner);

        // Define sorting options array and set ArrayAdapter to the Spinner
        String[] sortingOptions = {"Most Duration", "Recent", "Oldest"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, sortingOptions);
        sortingOptionsSpinner.setAdapter(spinnerAdapter);
        // Create an instance of StatRecycler
        statRecycler = new StatRecycler(this, exerciseStatsList, this);
        recyclerView.setAdapter(statRecycler); // Set the adapter to the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Access the Room database and retrieve data

        // Access the Room database and retrieve data


        // Retrieve data asynchronously using databaseWriteExecutor
        // Fetch data from RoutesStats table asynchronously


            // Perform UI updates or further operations with the fetched data if needed

        sortingOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Most Duration option
                        // Call the sorting method in the adapter when "Most Duration" is selected
                        statRecycler.sortByMostDuration();
                        break;
                    case 1: // Recent option
                        // Handle other sorting options if needed
                        statRecycler.sortByNewestDate();

                        break;
                    case 2: // Oldest option
                        // Handle other sorting options if needed
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }


    private void displayDataFragment() {
        // Create an instance of the DisplayDataFragment
        DisplayDataFragment dataFragment = new DisplayDataFragment();

        // Replace the current container with the DisplayDataFragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.map_fragment_container, dataFragment) // Replace with your fragment container ID
                .addToBackStack(null) // Optional: Add the transaction to the back stack
                .commit();
    }
    private void SetUpModel() {
        StatDatabase.databaseWriteExecutor.execute(() -> {
            exerciseStatsList = statDAO.getAllExerciseStats();

            runOnUiThread(() -> {
                statRecycler.setData(exerciseStatsList); // Set data to the adapter
                printExerciseStats(exerciseStatsList); // Print data to Logcat
            });
        });
    }

    // Method to print ExerciseStats data to Logcat
    private void printExerciseStats(List<ExerciseStats> exerciseStatsList) {
        for (ExerciseStats exerciseStats : exerciseStatsList) {
            Log.d("ExerciseStats", "Exercise: " + exerciseStats.getExercise() +
                    ", Duration: " + exerciseStats.getDuration() +
                    ", Coordinates: " + exerciseStats.getCoordinates());

            List<double[]> coordinates = exerciseStats.getCoordinates();
            for (double[] coordinate : coordinates) {
                Log.d("ExerciseStats", "   Latitude: " + coordinate[0] + ", Longitude: " + coordinate[1]);
            }
        }


    }

    @Override
    public void onRouteClick(int position) {
        // Get the clicked exercise stats
        ExerciseStats clickedExercise = exerciseStatsList.get(position);

        if (clickedExercise != null) {
            // Create a new instance of the DisplayExercise fragment
            DisplayExercise displayExerciseFragment = new DisplayExercise();

            // Pass the coordinates to the fragment using Bundle
            Bundle bundle = new Bundle();
            bundle.putSerializable("coordinates", new ArrayList<>(clickedExercise.getCoordinates()));
            displayExerciseFragment.setArguments(bundle);

            // Open the fragment by replacing the current container with the DisplayExercise fragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_fragment_container, displayExerciseFragment) // Pass the fragment instance
                    .addToBackStack(null) // Optional: Add the transaction to the back stack
                    .commit();
        }
    }

    @Override
    public void onEditClick(int position) {
        // Get the clicked exercise stats
        ExerciseStats clickedExercise = exerciseStatsList.get(position);

        // Create an AlertDialog for user input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter New Exercise Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newExerciseName = input.getText().toString(); // Retrieve entered exercise name

                if (!TextUtils.isEmpty(newExerciseName)) {
                    // Update exercise name in the database
                    StatDatabase.databaseWriteExecutor.execute(() -> {
                        // Get the current exercise name
                        String oldExerciseName = clickedExercise.getExercise();

                        // Update exercise name in the database
                        statDAO.updateExerciseName(oldExerciseName, newExerciseName);

                        // Fetch updated data and refresh RecyclerView
                        exerciseStatsList = statDAO.getAllExerciseStats();
                        runOnUiThread(() -> {
                            statRecycler.setData(exerciseStatsList);
                        });
                    });
                } else {
                    // Handle case where the entered name is empty
                    Toast.makeText(StatPage.this, "Please enter a valid exercise name", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show(); // Show the AlertDialog to capture user input
    }

}