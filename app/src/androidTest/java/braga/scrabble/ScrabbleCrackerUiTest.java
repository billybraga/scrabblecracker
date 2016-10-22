package braga.scrabble;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import junit.framework.Assert;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.view.View.VISIBLE;

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
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        ListView listView = (ListView)view;
                        TwoLineListItem item = (TwoLineListItem) listView.getAdapter().getView(0, null, null);
                        Assert.assertEquals("horse", ((TextView)item.getChildAt(0)).getText());
                    }
                })
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
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        ListView listView = (ListView)view;
                        TwoLineListItem item = (TwoLineListItem) listView.getAdapter().getView(0, null, null);
                        Assert.assertEquals("horse", ((TextView)item.getChildAt(0)).getText());
                    }
                })
                .check (matches (braga.utils.Matchers.withListLargerThanSize(0)));
    }

    @Test
    public void resultClickTest() {
        // Type text and then press the button.
        onView(withId(R.id.editTextLetters))
                .perform(typeText("hrse"), closeSoftKeyboard());
        onView(withId(R.id.editTextBoardLetters))
                .perform(typeText("o"), closeSoftKeyboard());
        onView(withId(R.id.buttonSolve)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.listViewResults))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        ListView listView = (ListView)view;
                        TwoLineListItem item = (TwoLineListItem) listView.getAdapter().getView(0, null, null);
                        Assert.assertEquals("horse", ((TextView)item.getChildAt(0)).getText());
                    }
                })
                .perform(clickItem(0, R.id.listViewResults))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        String word = (String)((HashMap)((ListView)view).getAdapter().getItem(0)).get(("Word"));
                        ClipData data = ((ClipboardManager) mActivityRule.getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip();
                        Assert.assertEquals ("Scrabble Word Finder", data.getDescription().getLabel());
                        Assert.assertEquals (word, data.getItemAt(0).getText());
                    }
                });

        onView(withId(R.id.selectionLayout))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(View.VISIBLE, view.getVisibility());
                    }
                });

        onView(withId(R.id.selectionWebView))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        WebView webView = (WebView)view;
                        Assert.assertTrue(webView.getUrl().contains("wiktionary"));
                    }
                });

        onView(withId(R.id.activity_scrabble_cracker))
                .perform(pressBack());

        onView(withId(R.id.selectionLayout))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(View.INVISIBLE, view.getVisibility());
                    }
                });

        onView(withId(R.id.buttonSolve)).perform(click());

        // Check that the text was changed.
        onView(withId(R.id.listViewResults))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        ListView listView = (ListView)view;
                        TwoLineListItem item = (TwoLineListItem) listView.getAdapter().getView(0, null, null);
                        Assert.assertEquals("horse", ((TextView)item.getChildAt(0)).getText());
                    }
                })
                .perform(clickItem(0, R.id.listViewResults));

        onView(withId(R.id.selectionLayout))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(View.VISIBLE, view.getVisibility());
                    }
                });

        onView(withId(R.id.selectionLayout))
                .perform(click())
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(View.INVISIBLE, view.getVisibility());
                    }
                });
    }

    private ViewAction clickItem(int position, long id) {
        return new ItemClickViewAction(position, id);
    }

    public class ItemClickViewAction implements ViewAction {
        private final int position;
        private final long id;

        public ItemClickViewAction(int position, long id) {
            this.position = position;
            this.id = id;
        }

        @Override
        public Matcher<View> getConstraints() {
            return new BaseMatcher<View>() {
                @Override
                public void describeTo(Description description) {

                }

                @Override
                public boolean matches(Object item) {
                    return item instanceof ListView;
                }
            };
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void perform(UiController uiController, View view) {
            ((ListView)view).performItemClick(view, this.position, this.id);
        }
    }
}
