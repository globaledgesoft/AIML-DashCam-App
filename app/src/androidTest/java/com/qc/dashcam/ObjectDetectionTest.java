package com.qc.dashcam;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.qc.dashcam.Activity.MainActivity;
import com.qc.dashcam.CommonUtil.Logger;
import com.qc.dashcam.Helpers.Box;
import com.qc.dashcam.Helpers.SNPEHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class ObjectDetectionTest {

    private static final String TAG = "GE_" + ObjectDetectionTest.class.getSimpleName();
    private Context instrumentationCtx;
    private MainActivity mainActivity;
    private SNPEHelper mSNPEHelper;
    private Bitmap bMap, compressedBitmap;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        instrumentationCtx = InstrumentationRegistry.getTargetContext();
        mainActivity = mActivityTestRule.getActivity();
        mSNPEHelper = new SNPEHelper(mainActivity.getApplication());
        setPersonBitmap();

    }

    /**
     * This method sets the bitmap by taking input image from drawable folder
     */
    public void setPersonBitmap() {
        bMap = BitmapFactory.decodeResource(mainActivity.getResources(), R.drawable.person, null);
        bMap = Bitmap.createScaledBitmap(bMap, 300, 300, false);
        Logger.d(TAG, "bMap...." + bMap.getHeight() + "   " + bMap.getWidth());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bMap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                byteArray.length);
    }

    /**
     * Positive test case to check if the neural network is established or not
     */
    @Test
    public void Android_UT_test_isNetworkConnected_positive() {
        mSNPEHelper.loadMobileNetSSDFromAssets();
        assertTrue(mSNPEHelper.loadMobileNetSSDFromAssets());
    }

    /**
     * Negative test case to check if the neural network is established or not
     */
    @Test
    public void Android_UT_test_isNetworkConnected_negative() {
        ArrayList<Box> boxes = mSNPEHelper.mobileNetSSDInference(compressedBitmap);
        assertEquals(null, boxes);
    }


    /**
     * Positive test case to check if the object is predicted properly or not by mobilenet model
     */
    @Test
    public void Android_UT_testObjectDetection_positive() {
        mSNPEHelper.loadMobileNetSSDFromAssets();
        ArrayList<Box> boxes = mSNPEHelper.mobileNetSSDInference(compressedBitmap);
        Logger.d(TAG, "boxes...." + boxes.size());
        boolean isDetected = false;
        if (boxes.get(0).type_name != null && boxes.get(0).type_name.equals("person"))
            isDetected = true;
        assertTrue(isDetected);
    }

    /**
     * Negative test case to check if the object is predicted properly or not by mobilenet model
     */
    @Test
    public void Android_UT_testObjectDetection_negative() {
        mSNPEHelper.loadMobileNetSSDFromAssets();
        ArrayList<Box> boxes = mSNPEHelper.mobileNetSSDInference(compressedBitmap);
        Logger.d(TAG, "boxes...." + boxes.size());
        boolean isDetected = false;
        if (boxes.get(0).type_name != null && boxes.get(0).type_name.equals("bus"))
            isDetected = true;
        assertFalse(isDetected);
    }

    @After
    public void tearDown() {
        mSNPEHelper.disposeNeuralNetwork();
    }
}
