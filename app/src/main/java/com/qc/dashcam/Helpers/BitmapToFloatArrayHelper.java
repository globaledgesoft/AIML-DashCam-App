package com.qc.dashcam.Helpers;

import android.graphics.Bitmap;

import com.qc.dashcam.CommonUtil.Logger;

import java.nio.ByteBuffer;

public class BitmapToFloatArrayHelper {

    private static final String TAG = "GE_" + BitmapToFloatArrayHelper.class.getSimpleName();
    private ByteBuffer mByteBufferHW4;
    private float[] mFloatBufferHW3;
    private boolean mIsFloatBufferBlack;

    /**
     * This will assume the geometry of both buffers from the first input bitmap.
     */
    public void bitmapToBuffer(final Bitmap inputBitmap) {
        final int inputBitmapBytesSize = inputBitmap.getRowBytes() * inputBitmap.getHeight();
        if (mByteBufferHW4 == null || mByteBufferHW4.capacity() != inputBitmapBytesSize) {
            mByteBufferHW4 = ByteBuffer.allocate(inputBitmapBytesSize);
            mFloatBufferHW3 = new float[1 * inputBitmap.getWidth() * inputBitmap.getHeight() * 3];
        }
        mByteBufferHW4.rewind();
        Logger.d(TAG, "mFloatBufferHW3" + mFloatBufferHW3.length + "");
        inputBitmap.copyPixelsToBuffer(mByteBufferHW4);
    }

    /**
     * This will process pixels RGBA(0..255) to BGR(-1..1)
     */
    public float[] bufferToNormalFloatsBGR() {
        // Pre-processing as per: https://confluence.qualcomm.com/confluence/display/ML/Preprocessing+for+Inference
        final byte[] inputArrayHW4 = mByteBufferHW4.array();
        final int area = mFloatBufferHW3.length / 3;
        long sumG = 0;
        int srcIdx = 0, dstIdx = 0;
        final float inputScale = 0.00784313771874f;
        for (int i = 0; i < area; i++) {
            // NOTE: the 0xFF a "cast" to unsigned int (otherwise it will be negative numbers for bright colors)
            final int pixelR = inputArrayHW4[srcIdx] & 0xFF;
            final int pixelG = inputArrayHW4[srcIdx + 1] & 0xFF;
            final int pixelB = inputArrayHW4[srcIdx + 2] & 0xFF;
            mFloatBufferHW3[dstIdx] = inputScale * (float) pixelB - 1;
            mFloatBufferHW3[dstIdx + 1] = inputScale * (float) pixelG - 1;
            mFloatBufferHW3[dstIdx + 2] = inputScale * (float) pixelR - 1;
            srcIdx += 4;
            dstIdx += 3;
            sumG += pixelG;
        }
        // the buffer is black if on average on average Green < 13/255 (aka: 5%)
        mIsFloatBufferBlack = sumG < (area * 13);
        return mFloatBufferHW3;
    }

    boolean isFloatBufferBlack() {
        return mIsFloatBufferBlack;
    }
}
