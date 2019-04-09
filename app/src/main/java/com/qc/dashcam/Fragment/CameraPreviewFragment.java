
package com.qc.dashcam.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.qc.dashcam.CommonUtil.Constants;
import com.qc.dashcam.CommonUtil.Logger;
import com.qc.dashcam.Helpers.Box;
import com.qc.dashcam.Helpers.OverlayRenderer;
import com.qc.dashcam.Helpers.SNPEHelper;
import com.qc.dashcam.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class CameraPreviewFragment extends Fragment implements
        TextureView.SurfaceTextureListener {

    private static final String TAG = "GE_" + CameraPreviewFragment.class.getSimpleName();
    private CameraManager mCameraManager;
    private String[] ids;
    private SurfaceTexture mSurfaceTexture;
    private CameraCallback mCameraCallback;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureRequest.Builder builder;
    private CameraDevice mCameraDevice;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private TextureView mTextureView;
    private SNPEHelper mSnpeHelper;
    private boolean mNetworkLoaded;
    private Bitmap mModelInputBitmap;
    private Canvas mModelInputCanvas;
    private Paint mModelBitmapPaint;
    private OverlayRenderer mOverlayRenderer;
    private String searchLabel;
    private boolean mInferenceSkipped;

    public static CameraPreviewFragment create() {
        final CameraPreviewFragment fragment = new CameraPreviewFragment();
        return fragment;
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView = (TextureView) view.findViewById(R.id.surface);
        mTextureView.setSurfaceTextureListener(this);
        mOverlayRenderer = view.findViewById(R.id.overlayRenderer);

        mCameraManager = (CameraManager) getActivity().getApplicationContext().
                getSystemService(Context.CAMERA_SERVICE);
        mCameraCallback = new CameraCallback();


        if (mCameraManager != null) {
            try {
                ids = mCameraManager.getCameraIdList();
            } catch (CameraAccessException ex) {
                ex.printStackTrace();
            }

        } else {
            Logger.e(TAG, "Camera Service null");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Logger.d(TAG, "OnSurfaceTextureAvailable");
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Logger.d(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Logger.d(TAG, "onSurfaceTextureDestroyed");
        mCameraCaptureSession.close();
        mCameraDevice.close();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Logger.d(TAG, "onSurfaceTextureUpdated");
    }

    /**
     * Method to open rear camera
     */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            mCameraManager.openCamera(ids[0], mCameraCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to create camera preview with texture view
     */
    private void createCameraPreview() {
        mSurfaceTexture = mTextureView.getSurfaceTexture();
        Surface mSurface = new Surface(mSurfaceTexture);

        try {
            Logger.d(TAG, "createCaptureSession");
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface), new CameraCapture(),
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        try {
            Logger.d(TAG, "createCaptureRequest");
            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mSurface);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        startBackgroundThread();
        ensureNetCreated();

    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    public class CameraCallback extends CameraDevice.StateCallback {

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            Logger.d(TAG, "onClosed()");
            super.onClosed(camera);
        }

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Logger.d(TAG, "onOpened()");
            mCameraDevice = cameraDevice;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Logger.d(TAG, "onDisconnected()");
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            switch (i) {
                case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                    Logger.e(TAG, "Error in Camera Device");
                    break;
                case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                    Logger.e(TAG, "Camera Device is disabled");
                    break;
                case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                    Logger.e(TAG, "Camera Device is already in use");
                    break;
                case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                    Logger.e(TAG, "Error in Camera Service");
                    break;
                case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                    Logger.e(TAG, "Error for MAX Cameras");
                    break;
                default:
                    Logger.e(TAG, "default error");
                    break;
            }
        }
    }

    public class CameraCapture extends android.hardware.camera2.CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull android.hardware.camera2.CameraCaptureSession
                                         cameraCaptureSession) {
            Logger.d(TAG, "OnConfigured");
            mCameraCaptureSession = cameraCaptureSession;

            try {
                cameraCaptureSession.setRepeatingRequest(builder.build(), new CameraSession(),
                        mBackgroundHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull android.hardware.camera2.CameraCaptureSession
                                              cameraCaptureSession) {
            Logger.d(TAG, "OnConfiguredFailed");
        }

    }

    private class CameraSession extends android.hardware.camera2.CameraCaptureSession.CaptureCallback {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Logger.d(TAG, "onCaptureCompleted :");
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            if (mNetworkLoaded) {
                Bitmap mBitmap = mTextureView.getBitmap(Constants.BITMAP_WIDTH, Constants.BITMAP_HEIGHT);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] byteArray = stream.toByteArray();

                Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                        byteArray.length);

                final int inputWidth = mSnpeHelper.getInputTensorWidth();
                final int inputHeight = mSnpeHelper.getInputTensorHeight();

                if (mModelInputBitmap == null || mModelInputBitmap.getWidth() != inputWidth || mModelInputBitmap.getHeight() != inputHeight) {
                    // create ARGB8888 bitmap and canvas, with the right size
                    mModelInputBitmap = Bitmap.createBitmap(inputWidth, inputHeight, Bitmap.Config.ARGB_8888);
                    mModelInputCanvas = new Canvas(mModelInputBitmap);
                    mModelInputCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    // compute the roto-scaling matrix (preview image -> screen image) and apply it to
                    // the canvas. this includes translation for 'letterboxing', i.e. the image will
                    // have black bands to the left and right if it's a portrait picture
                    final Matrix mtx = new Matrix();
                    final int previewWidth = compressedBitmap.getWidth();
                    final int previewHeight = compressedBitmap.getHeight();
                    final float scaleWidth = ((float) inputWidth) / previewWidth;
                    final float scaleHeight = ((float) inputHeight) / previewHeight;
                    final float frameScale = Math.min(scaleWidth, scaleHeight); // centerInside
                    //final float frameScale = Math.max(scaleWidth, scaleHeight); // centerCrop
                    final float dy = inputWidth - (previewWidth * frameScale);
                    final float dx = inputHeight - (previewHeight * frameScale);
                    mtx.postScale(frameScale, frameScale);
                    mtx.postTranslate(dx / 2, dy / 2);
                    if (rotation != 0) {
                        mtx.postTranslate(-inputWidth / 2, -inputHeight / 2);
                        mtx.postRotate(-rotation);
                        mtx.postTranslate(inputWidth / 2, inputHeight / 2);
                    }
                    mModelInputCanvas.setMatrix(mtx);

                    // create the "Paint", to set the antialiasing option
                    mModelBitmapPaint = new Paint();
                    mModelBitmapPaint.setFilterBitmap(true);

                }
                mModelInputCanvas.drawColor(Color.BLACK);
                mModelInputCanvas.drawBitmap(compressedBitmap, 0, 0, mModelBitmapPaint);
                final ArrayList<Box> boxes = mSnpeHelper.mobileNetSSDInference(mModelInputBitmap);

                // [2-45ms] give the bitmap to SNPE for inference
                mInferenceSkipped = boxes == null;

                if (!mInferenceSkipped) {
                    Logger.d(TAG, "mInferenceSkipped...." + mInferenceSkipped);
                    HashSet<String> nearStringsSet = new HashSet<>();
                    for (Box box : boxes) {
                        String textLabel = (box.type_name != null && !box.type_name.isEmpty()) ? box.type_name : String.valueOf(box.type_id);
                        Logger.d(TAG, "type_score...." + box.type_score);
                        if (box.type_score < 0.8)
                            continue;
                        nearStringsSet.add(textLabel);
                        Logger.d(TAG, "objects" + textLabel);
                        if (searchLabel != null && textLabel.toLowerCase().contains(searchLabel.toLowerCase())) {
                            String nearObjects = "";
                            int count = 0;
                            for (String word : nearStringsSet) {
                                if (!searchLabel.contains(word) && count <= 2) {
                                    nearObjects += word + ", ";
                                }
                                count++;
                            }
                            Logger.d(TAG, searchLabel);
                            searchLabel = null;
                            break;
                        }
                    }
                }
                // deep copy the results so we can draw the current set while guessing the next set
                mOverlayRenderer.setBoxesFromAnotherThread(boxes);
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Logger.e(TAG, "onCaptureFailed");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Logger.d(TAG, "onCaptureProgressed");
        }

        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull
                CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Logger.d(TAG, "onCaptureStarted");
        }

    }

    /**
     * Method to ensure if neural network is loaded
     *
     * @return
     */
    private boolean ensureNetCreated() {
        if (mSnpeHelper == null) {
            // load the neural network for object detection with SNPE
            mSnpeHelper = new SNPEHelper(getActivity().getApplication());
            mNetworkLoaded = mSnpeHelper.loadMobileNetSSDFromAssets();
            Logger.d(TAG, " ensureNetCreated  " + mNetworkLoaded);
        }
        return mNetworkLoaded;
    }


}