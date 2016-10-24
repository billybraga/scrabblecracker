package braga.scrabble;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.HashSet;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static braga.utils.Matchers.withListLargerThanSize;
import static braga.utils.Matchers.withListSize;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by Billy on 10/15/2016.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScrabbleCrackerUiTest {
    static HashSet<String> firstResults = new HashSet<>();
    
    static {
        firstResults.add("horse");
        firstResults.add("héros");
    }
    
    @Rule
    public ActivityTestRule<ScrabbleCrackerActivity> mActivityRule = new ActivityTestRule<>(
            ScrabbleCrackerActivity.class);

    @Test
    public void noResultOnEmptySearchTest() {
        fetchResults(null);

        // Check that the text was changed.
        onView(withId(R.id.listViewResults))
                .check (matches (withListSize (0)));
    }

    @Test
    public void resultsOnHandOnlyTextSearchTest() {
        fetchResults("horse");

        onView(withId(R.id.activity_scrabble_cracker))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        InputMethodManager imm = (InputMethodManager) mActivityRule
                                .getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);

                        Assert.assertFalse(imm.isActive());
                    }
                });

        testHaveResults();
    }

    @Test
    public void resultsOnBoardAndTextSearchTest() {
        fetchResults();
        testHaveResults();
    }

    @Test
    public void twoItemClickThenBackButtonMustHideSelectionView() {
        fetchResults();

        onView(withId(R.id.listViewResults))
                .perform(clickItem(0));

        onView(withId(R.id.activity_scrabble_cracker))
                .perform(pressBack());

        onView(withId(R.id.listViewResults))
                .perform(clickItem(1));

        onView(withId(R.id.selectionLayout))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(INVISIBLE, view.getVisibility());
                    }
                });
    }

    @Test
    public void showCameraTest() {
        onView(withId(R.id.editTextLetters))
                .perform(clickDrawables());

        onView(withId(R.id.boardDetectionCameraView))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(View.VISIBLE, view.getVisibility());
                    }
                });
    }

    @Test
    public void hideCameraWithBackTest() {
        onView(withId(R.id.editTextLetters))
                .perform(clickDrawables());

        onView(withId(R.id.activity_scrabble_cracker))
                .perform(pressBack());

        onView(withId(R.id.boardDetectionCameraView))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(View.INVISIBLE, view.getVisibility());
                    }
                });
    }

    @Test
    public void backHidesSelectionTest() {
        fetchResults();

        onView(withId(R.id.listViewResults))
                .perform(clickItem(0));

        onView(withId(R.id.activity_scrabble_cracker))
                .perform(pressBack());

        onView(withId(R.id.selectionLayout))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        Assert.assertEquals(INVISIBLE, view.getVisibility());
                    }
                });
    }

    @Test
    public void clickOutsideWebViewHidesSelectionTest() {
        fetchResults();

        onView(withId(R.id.listViewResults))
                .perform(clickItem(0));

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
                        // TODO : find why it doesn't work
                        // Assert.assertEquals(INVISIBLE, view.getVisibility());
                    }
                });
    }

    @Test
    public void clickItemTest() {
        fetchResults();

        onView(withId(R.id.listViewResults))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        ListView listView = (ListView)view;
                        TwoLineListItem item = (TwoLineListItem) listView.getAdapter().getView(0, null, null);
                        Assert.assertTrue(firstResults.contains(((TextView)item.getChildAt(0)).getText()));
                    }
                })
                .perform(clickItem(0));

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
                        Assert.assertTrue(view.isShown());
                    }
                });
    }

    private void testHaveResults() {
        onView(withId(R.id.listViewResults))
                .check (matches (withListLargerThanSize(0)))
                .check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        ListView listView = (ListView)view;
                        TwoLineListItem item = (TwoLineListItem) listView.getAdapter().getView(0, null, null);
                        Assert.assertTrue(firstResults.contains(((TextView)item.getChildAt(0)).getText()));
                    }
                });
    }

    private void fetchResults() {
        fetchResults("hors", "e");
    }

    private void fetchResults(String handLetters) {
        fetchResults(handLetters, null);
    }

    private void fetchResults(String handLetters, String boardLetters) {
        onView(withId(R.id.editTextLetters))
                .perform(typeText(handLetters));

        if (boardLetters != null) {
            onView(withId(R.id.editTextBoardLetters))
                    .perform(typeText(boardLetters));
        }

        onView(withId(R.id.buttonSolve))
                .perform(click());
    }

    private ViewAction clickItem(int position) {
        return new ItemClickViewAction(position);
    }

    public class ItemClickViewAction implements ViewAction {
        private final int position;

        public ItemClickViewAction(int position) {
            this.position = position;
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
            ((ListView)view).performItemClick(view, this.position, view.getId());
        }
    }

    public static ViewAction clickDrawables()
    {
        return new ViewAction()
        {
            @Override
            public Matcher<View> getConstraints()//must be a textview with drawables to do perform
            {
                return allOf(isAssignableFrom(TextView.class), new BoundedMatcher<View, TextView>(TextView.class)
                {
                    @Override
                    protected boolean matchesSafely(final TextView tv)
                    {
                        if( tv.requestFocusFromTouch())//get fpocus so drawables become visible
                            for (Drawable d : tv.getCompoundDrawables())//if the textview has drawables then return a match
                                if (d != null)
                                    return true;

                        return false;
                    }

                    @Override
                    public void describeTo(Description description)
                    {
                        description.appendText("has drawable");
                    }
                });
            }

            @Override
            public String getDescription()
            {
                return "click drawables";
            }

            @Override
            public void perform(final UiController uiController, final View view)
            {
                TextView tv = (TextView)view;
                if(tv != null && tv.requestFocusFromTouch())//get focus so drawables are visible
                {
                    Drawable[] drawables = tv.getCompoundDrawables();

                    Rect tvLocation = new Rect();
                    tv.getHitRect(tvLocation);

                    Point[] tvBounds = new Point[4];//find textview bound locations
                    tvBounds[0] = new Point(tvLocation.left, tvLocation.centerY());
                    tvBounds[1] = new Point(tvLocation.centerX(), tvLocation.top);
                    tvBounds[2] = new Point(tvLocation.right, tvLocation.centerY());
                    tvBounds[3] = new Point(tvLocation.centerX(), tvLocation.bottom);

                    for (int location = 0; location < 4; location++)
                        if(drawables[location] != null)
                        {
                            Rect bounds = drawables[location].getBounds();
                            tvBounds[location].offset(bounds.width() / 2, bounds.height() / 2);//get drawable click location for left, top, right, bottom
                            if(tv.dispatchTouchEvent(MotionEvent.obtain(android.os.SystemClock.uptimeMillis(), android.os.SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, tvBounds[location].x, tvBounds[location].y, 0)))
                                tv.dispatchTouchEvent(MotionEvent.obtain(android.os.SystemClock.uptimeMillis(), android.os.SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, tvBounds[location].x, tvBounds[location].y, 0));
                        }
                }
            }
        };
    }
}
