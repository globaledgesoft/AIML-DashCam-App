package com.qc.dashcam.Helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.camera2.params.Face;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;

import com.qc.dashcam.CommonUtil.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Renderer for the image detection box
 */
public class OverlayRenderer extends View {

    private static final String TAG = OverlayRenderer.class.getSimpleName();
    private ReentrantLock mLock = new ReentrantLock();
    private ArrayList<Box> mBoxes = new ArrayList<>();
    private boolean mHasResults;
    private final Map<Integer, Integer> mColorIdxMap = new ArrayMap<>();
    private Paint mOutlinePaint = new Paint();
    private Paint mCenterPaint = new Paint();
    private Paint mTextPaint = new Paint();
    private float mBoxScoreThreshold = 0.4f;

    public OverlayRenderer(Context context) {
        super(context);
        init();
    }

    public OverlayRenderer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayRenderer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setBoxesFromAnotherThread(ArrayList<Box> nextBoxes) {
        mLock.lock();
        delete();
        if (nextBoxes == null) {
            mHasResults = false;
            for (Box box : mBoxes)
                box.type_score = 0;
        } else {

            mHasResults = true;
            for (int i = 0; i < nextBoxes.size(); i++) {
                final Box otherBox = nextBoxes.get(i);
                if (i >= mBoxes.size()) {
                    mBoxes.add(new Box());
                }
                otherBox.copyTo(mBoxes.get(i));
            }
        }
        mLock.unlock();
        postInvalidate();
    }


    private void init() {
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(6);
        mCenterPaint.setColor(Color.RED);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(45);
        mTextPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mLock.lock();
        for (int i = 0; i < mBoxes.size(); i++) {
            final Box box = mBoxes.get(i);
            // skip rendering below the threshold
            if (box.type_score < mBoxScoreThreshold)
                break;

            if (box.type_id == 2 || box.type_id == 6 || box.type_id == 7 || box.type_id == 14 || box.type_id == 15)
                computeFinalGeometry(box, canvas);
        }
        mLock.unlock();
    }

    private void computeFinalGeometry(Box box, Canvas canvas) {
        Logger.d(TAG, "Boxes :" + box.type_name);
        // compute the final geometry
        Logger.d(TAG, box.type_name);

        final int viewWidth = getWidth();
        final int viewHeight = getHeight();

        float y = viewHeight * box.left;
        float x = viewWidth * box.top;
        float y1 = viewHeight * box.right;
        float x1 = viewWidth * box.bottom;

        // draw the text
        String textLabel = (box.type_name != null && !box.type_name.isEmpty()) ? box.type_name : String.valueOf(box.type_id + 2);
        canvas.drawText(textLabel, x + 10, y + 30, mTextPaint);
        // draw the box
        mOutlinePaint.setColor(colorForIndex(box.type_id));
        canvas.drawRect(x, y, x1, y1, mOutlinePaint);
    }

    private int colorForIndex(int index) {
        // create color on the fly if missing
        if (!mColorIdxMap.containsKey(index)) {
            float[] hsv = {(float) (Math.random() * 360), (float) (0.5 + Math.random() * 0.5), (float) (0.5 + Math.random() * 0.5)};
            mColorIdxMap.put(index, Color.HSVToColor(hsv));
        }
        return mColorIdxMap.get(index);
    }

    public void delete() {
        mOutlinePaint.setColor(Color.TRANSPARENT);
        postInvalidate();
    }

}
