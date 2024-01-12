package com.example.gps_locatorcw;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.example.gps_locatorcw.activities.MainActivity;

@RunWith(AndroidJUnit4.class)
public class MainActivityJUNIT {

    private ActivityScenario<MainActivity> activityScenario;

    @Before
    public void setUp() {
        // Launch the MainActivity before each test
        activityScenario = ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void tearDown() {
        // Close the MainActivity after each test
        activityScenario.close();
    }

    @Test
    public void testMapFragmentVisibility() {
        Espresso.onView(ViewMatchers.withTagValue(Matchers.is("statFragment"))).check(matches(isDisplayed()));

    }

//    @Test
//    public void testToMapsButtonClick() {
//        // Simulate a click on the "toMaps" ImageView
//        Espresso.onView(withId(R.id.toMaps)).perform(click());
//
//        // Check if the MapFragment is displayed after the click
//        Espresso.onView(withId(R.id.map_fragment_container)).check(matches(isDisplayed()));
//    }

    // Add more tests for other UI interactions as needed
}
