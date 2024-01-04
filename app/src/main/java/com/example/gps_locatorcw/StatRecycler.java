package com.example.gps_locatorcw;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StatRecycler extends RecyclerView.Adapter<StatRecycler.StatViewHolder> {
     List<ExerciseStats> exerciseStats;
    private Context context;

    static ImageView editExerciseButton;

    private final RouteClick mpInterface;


    public StatRecycler(Context context, List<ExerciseStats> statList,RouteClick listener) {
        this.context = context;
        this.exerciseStats = statList;
        this.mpInterface = listener; // Assign the interface instance
    }

    @NonNull
    @Override
    public StatRecycler.StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);



        return new StatRecycler.StatViewHolder(view, mpInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull StatRecycler.StatViewHolder holder, int position) {
        // Bind data to your views in MyViewHolder
        holder.name.setText(exerciseStats.get(position).getExercise());
        holder.duration.setText(String.valueOf(exerciseStats.get(position).getDuration()));

        holder.distance.setText(exerciseStats.get(position).getDistance());
        holder.avgpace.setText(exerciseStats.get(position).getAvgpace());


        Log.d("Madsd", "Exercise;f" + exerciseStats.get(position).getExercise());
        Log.d("Madsd", "Duration;f" + exerciseStats.get(position).getDuration());


    }

    public void sortByMostDuration() {
        // Sort exerciseStatsList by the most duration
        Collections.sort(exerciseStats, new Comparator<ExerciseStats>() {
            @Override
            public int compare(ExerciseStats o1, ExerciseStats o2) {
                // Sort in descending order based on duration
                return Double.compare(o2.getDuration(), o1.getDuration());
            }
        });
        notifyDataSetChanged(); // Notify the adapter after sorting
    }

    public void sortByNewestDate() {
        // Reverse the exerciseStatsList to sort from newest to oldest
        Collections.reverse(exerciseStats);
        notifyDataSetChanged(); // Notify the adapter after reversing
    }
    @Override
    public int getItemCount() {

        Log.d("Helloooo", String.valueOf(exerciseStats.size()));
        return exerciseStats.size();
    }

    public void setData(List<ExerciseStats> newList) {
        if (newList != null) {
            // Update the existing data list
            exerciseStats.clear(); // Clear the existing list
            exerciseStats.addAll(newList); // Add all items from newList
            notifyDataSetChanged(); // Notify the adapter that the data has changed
        }
    }


    public static class StatViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView duration;

        TextView avgpace;

        TextView distance;
        RouteClick mpInterface;

        public StatViewHolder(View itemView, RouteClick mpInterface) {
            super(itemView);
            name = itemView.findViewById(R.id.name); // Replace with your TextView IDs
            duration = itemView.findViewById(R.id.duration); // Replace with your TextView IDs
            editExerciseButton = itemView.findViewById(R.id.editNotes);

            avgpace = itemView.findViewById(R.id.pace);
            distance= itemView.findViewById(R.id.timed);

// Attach a click listener to the entire row view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mpInterface != null) {
                        mpInterface.onRouteClick(position);
                        Log.d("Hdphsapoc", "sdbisoidchi");
                    }
                }
            });


            // Set click listener for the edit button
            editExerciseButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mpInterface != null) {
                    mpInterface.onEditClick(position); // Notify activity about edit click
                }
            });



        }

    }
}
