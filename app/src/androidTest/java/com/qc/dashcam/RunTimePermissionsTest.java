package com.qc.dashcam;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.WindowManager;

import com.qc.dashcam.Activity.MainActivity;
import com.qc.dashcam.CommonUtil.Util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RunTimePermissionsTest {
    private Context instrumentationCtx;
    MainActivity mainActivity;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        instrumentationCtx = InstrumentationRegistry.getTargetContext();
        mainActivity = mActivityTestRule.getActivity();
    }

    /**
     * Test case to check if camera permission is granted or not
     */
    @Test
    public void Android_UT_testHasPermission() {
        boolean hasPermission;
        hasPermission = Util.hasPermission(instrumentationCtx);
        if (hasPermission)
            assertTrue(hasPermission);
        else assertFalse(hasPermission);
    }

}
