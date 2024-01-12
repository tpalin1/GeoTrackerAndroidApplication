package com.example.gps_locatorcw.databases;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gps_locatorcw.databases.DAO.StatDAO;
import com.example.gps_locatorcw.databases.StatDatabase;
import com.example.gps_locatorcw.databases.entities.ExerciseStats;

public class ExerciseProvider extends ContentProvider {

    public static final int EXERCISE_STATS_TABLE = 1;

    /** The authority of this content provider. */
    public static final String AUTHORITY = "com.example.gps_locatorcw.databases.ExerciseProvider";
    private StatDAO statDAO;

    public static final Uri URI_CHEESE = Uri.parse(
            "content://" + AUTHORITY + "/" + "user_stattable");
    /** The match code for some items in the Cheese table. */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int CODE_EXERCISE_STATS_DIR = 1;

    static {
        sUriMatcher.addURI(AUTHORITY, "user_stattable", EXERCISE_STATS_TABLE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }


    /**
     * @param uri           The URI to query. This will be the full URI sent by the client;
     *                      if the client is requesting a specific record, the URI will end in a record number
     *                      that the implementation should parse and add to a WHERE or HAVING clause, specifying
     *                      that _id value.
     * @param projection    The list of columns to put into the cursor. If
     *                      {@code null} all columns are included.
     * @param selection     A selection criteria to apply when filtering rows.
     *                      If {@code null} then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by
     *                      the values from selectionArgs, in order that they appear in the selection.
     *                      The values will be bound as Strings.
     * @param sortOrder     How the rows in the cursor should be sorted.
     *                      If {@code null} then the provider is free to define the sort order.
     * @return
     */
   /*Create the query code for the content provider utilisingt he statDAO methods*/
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final int code = sUriMatcher.match(uri);
        if (code == EXERCISE_STATS_TABLE) {
            final Context context = getContext();
            if (context == null) {
                return null;
            }
            StatDAO cheese = StatDatabase.getInstance(context).statDAO();
            Cursor cursor = null;
            if (code == CODE_EXERCISE_STATS_DIR){

                cursor = cheese.getAllExerciseStatsCursor();
                System.out.println("Query method - Cursor count: " + cursor.getCount());

            }
            cursor.setNotificationUri(context.getContentResolver(), uri);
            return cursor;
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


}

