package braga.scrabble;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Billy on 10/15/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScrabbleCrackerUiTest {
    @Rule
    public ActivityTestRule<ScrabbleCrackerActivity> mActivityRule = new ActivityTestRule<>(
            ScrabbleCrackerActivity.class);

    @Test
    public void noResultOnEmptySearchTest() {
        // Type text and then press the button.
        onView(withId(R.id.editTextLetters))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.editTextBoardLetters))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.buttonSolve)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.listViewResults))
                .check (matches (braga.utils.Matchers.withListSize (0)));
    }

    @Test
    public void resultsOnHandOnlyTextSearchTest() {
        // Type text and then press the button.
        onView(withId(R.id.editTextLetters))
                .perform(typeText("horse"), closeSoftKeyboard());
        onView(withId(R.id.editTextBoardLetters))
                .perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.buttonSolve)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.listViewResults))
                .check (matches (braga.utils.Matchers.withListLargerThanSize(0)));
    }

    @Test
    public void noResultsOnBoardOnlyTextSearchTest() {
        // Type text and then press the button.
        onView(withId(R.id.editTextLetters))
                .perform(typeText("hrse"), closeSoftKeyboard());
        onView(withId(R.id.editTextBoardLetters))
                .perform(typeText("o"), closeSoftKeyboard());
        onView(withId(R.id.buttonSolve)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.listViewResults))
                .check (matches (braga.utils.Matchers.withListLargerThanSize(0)));
    }
}
