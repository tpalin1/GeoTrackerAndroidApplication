package com.example.gps_locatorcw.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gps_locatorcw.R;
import com.example.gps_locatorcw.databases.DAO.StatDAO;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;
import com.example.gps_locatorcw.interfaces.RouteClick;

import java.util.ArrayList;
import java.util.List;

public class StatRecycler extends RecyclerView.Adapter<StatRecycler.StatViewHolder> {
     public static List<ExerciseStats> exerciseStats;
    private Context context;


    static ImageView editExerciseButton;

    public static List<ExerciseStats> filteredStats;
    private final RouteClick mpInterface;

    private StatDAO statDAO;


    /**
     * @param context The context of the activity
     * @param statList The list of stats to display
     * @param listener The interface instance
     *                 Constructor for the adapter
     */
    public StatRecycler(Context context, List<ExerciseStats> statList,RouteClick listener) {
        this.context = context;
        this.exerciseStats = statList;
        this.mpInterface = listener;
        this.filteredStats = new ArrayList<>(statList);
    }

    @NonNull
    @Override
    public StatRecycler.StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);



        return new StatRecycler.StatViewHolder(view, mpInterface);
    }


    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     *                 Called by RecyclerView to display the data at the specified position.
     */

    @Override
    public void onBindViewHolder(@NonNull StatRecycler.StatViewHolder holder, int position) {
        ExerciseStats currentStat = filteredStats.get(position);


        holder.name.setText(exerciseStats.get(position).getExercise());
        holder.duration.setText(String.valueOf(exerciseStats.get(position).getDuration()));

        holder.distance.setText(exerciseStats.get(position).getDistance());
        holder.avgpace.setText(exerciseStats.get(position).getAvgpace());





    }

    /**
     * @param filteredList The list of stats to filter
     *                     Creates a filtered list of stats
     */
    public void filterList(List<ExerciseStats> filteredList) {
        filteredStats = new ArrayList<>(filteredList);

        for (int i = 0; i < filteredStats.size(); i++) {
            Log.d("filteredStats", filteredStats.get(i).getExercise());
        }
        notifyDataSetChanged();
    }


    /**
     * @return The number of items in the data set held by the adapter.
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {

        return exerciseStats.size();
    }


    /**
     * @param newList The new list of stats
     *                Sets the data for the adapter and filters it
     */
    public void setData(List<ExerciseStats> newList) {
        exerciseStats = new ArrayList<>(newList);
        filterList(exerciseStats);
    }





    public static class StatViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView duration;

        TextView avgpace;

        TextView distance;
        RouteClick mpInterface;

        /**
         * @param itemView The view of the item
         * @param mpInterface  The interface instance
         *                     Constructor for the ViewHolder

         */
        public StatViewHolder(View itemView, RouteClick mpInterface) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            duration = itemView.findViewById(R.id.duration);
            editExerciseButton = itemView.findViewById(R.id.editNotes);

            avgpace = itemView.findViewById(R.id.pace);
            distance= itemView.findViewById(R.id.timed);


            itemView.setOnClickListener(new View.OnClickListener() {
                /**
                 * @param v The view that was clicked.
                 *          Called when a view has been clicked.
                 *
                 *          Shows the selected map route that the user took on that speciifc exercise.

                 */
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && mpInterface != null) {
                        ExerciseStats currentStat = filteredStats.get(position);
                        int filteredPosition = exerciseStats.indexOf(currentStat);

                        Log.d("Exercise clicked", exerciseStats.get(filteredPosition).getExercise());
                        mpInterface.onRouteClick(filteredPosition);
                    }
                }
            });





            editExerciseButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mpInterface != null) {
                    mpInterface.onEditClick(position);
                }
            });



        }

    }
}
