package com.example.gps_locatorcw;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Test;



import org.junit.runner.RunWith;

import static com.example.gps_locatorcw.databases.ExerciseProvider.AUTHORITY;



import static com.example.gps_locatorcw.databases.ExerciseProvider.EXERCISE_STATS_TABLE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.gps_locatorcw.databases.ExerciseProvider;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ContentProviderTest{

    private ContentResolver contentResolver;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        contentResolver = context.getContentResolver();
        System.out.println("ContentResolver: " + contentResolver.toString());
    }

    @Test
    public void testQueryUsers() {


        Uri usersUri = Uri.parse("content://" + AUTHORITY + "/user_stattable");
        System.out.println("Test Query URI: " + usersUri.toString());


        Cursor cursor = contentResolver.query(usersUri, null, null, null, null);
        assertNotNull("Cursor should not be null", cursor);
        assertTrue("Cursor should have at least one entry", cursor.getCount() > 0);
        cursor.close();
    }
}