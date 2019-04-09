package com.qc.dashcam.Helpers;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.ArrayMap;
import android.widget.Toast;

import com.qc.dashcam.CommonUtil.Logger;
import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("SameParameterValue")
public class SNPEHelper {
    private static final String TAG = SNPEHelper.class.getSimpleName();
    private final Application mApplication;
    private final Context mContext;
    private final BitmapToFloatArrayHelper mBitmapToFloatHelper;
    private final TimeStat mTimeStat;
    private String mSNPEVersionCached;


    // all of the following are allocated in the load function for the network
    private NeuralNetwork mNeuralNetwork;
    private String mRuntimeCoreName = "no core";
    private int[] mInputTensorShapeHWC;
    private FloatTensor mInputTensorReused;
    private Map<String, FloatTensor> mInputTensorsMap;

    public SNPEHelper(Application application) {
        mApplication = application;
        mContext = application;
        mBitmapToFloatHelper = new BitmapToFloatArrayHelper();
        mTimeStat = new TimeStat();
    }

    public String getSNPEVersion() {
        if (mSNPEVersionCached == null)
            mSNPEVersionCached = SNPE.getRuntimeVersion(mApplication);
        return mSNPEVersionCached;
    }

    public String getRuntimeCoreName() {
        return mRuntimeCoreName;
    }

    public int getInputTensorWidth() {
        return mInputTensorShapeHWC == null ? 0 : mInputTensorShapeHWC[1];
    }

    public int getInputTensorHeight() {
        return mInputTensorShapeHWC == null ? 0 : mInputTensorShapeHWC[2];
    }

    /* MobileNet-SSD Specific */

    private static final String MNETSSD_MODEL_ASSET_NAME = "caffe_mobilenet.dlc";
    private static final String MNETSSD_INPUT_LAYER = "data";
    private static final String MNETSSD_OUTPUT_LAYER = "detection_out";
    private static final boolean MNETSSD_NEEDS_CPU_FALLBACK = true;
    private static int MNETSSD_NUM_BOXES = 100;
    private final float[] floatOutput = new float[MNETSSD_NUM_BOXES * 7];
    private final ArrayList<Box> mSSDBoxes = Box.createBoxes(MNETSSD_NUM_BOXES);

    /**
     * This method loads model from assets and returns true if the model is loaded
     *
     * @return
     */
    public boolean loadMobileNetSSDFromAssets() {
        // cleanup
        disposeNeuralNetwork();

        // select core
        NeuralNetwork.Runtime selectedCore = NeuralNetwork.Runtime.GPU_FLOAT16;

        // load the network
        mNeuralNetwork = loadNetworkFromDLCAsset(mApplication, MNETSSD_MODEL_ASSET_NAME,
                selectedCore, MNETSSD_NEEDS_CPU_FALLBACK, MNETSSD_OUTPUT_LAYER);

        // if it didn't work, retry on CPU
        if (mNeuralNetwork == null) {
            complain("Error loading the DLC network on the " + selectedCore + " core. Retrying on CPU.");
            mNeuralNetwork = loadNetworkFromDLCAsset(mApplication, MNETSSD_MODEL_ASSET_NAME,
                    NeuralNetwork.Runtime.CPU, MNETSSD_NEEDS_CPU_FALLBACK, MNETSSD_OUTPUT_LAYER);
            if (mNeuralNetwork == null) {
                complain("Error also on CPU");
                return false;
            }
            complain("Loading on the CPU worked");
        }

        // cache the runtime name
        mRuntimeCoreName = mNeuralNetwork.getRuntime().toString();
        // read the input shape
        mInputTensorShapeHWC = mNeuralNetwork.getInputTensorsShapes().get(MNETSSD_INPUT_LAYER);
        // allocate the single input tensor
        mInputTensorReused = mNeuralNetwork.createFloatTensor(mInputTensorShapeHWC);
        // add it to the map of inputs, even if it's a single input
        mInputTensorsMap = new HashMap<>();
        mInputTensorsMap.put(MNETSSD_INPUT_LAYER, mInputTensorReused);
        return true;
    }

    public ArrayList<Box> mobileNetSSDInference(Bitmap modelInputBitmap) {
        try {
            // execute the inference, and get 3 tensors as outputs
            final Map<String, FloatTensor> outputs = inferenceOnBitmap(modelInputBitmap);
            if (outputs == null)
                return null;
            MNETSSD_NUM_BOXES = outputs.get(MNETSSD_OUTPUT_LAYER).getSize() / 7;
            Logger.d(TAG, "MNETSSD_NUM_BOXES   " + MNETSSD_NUM_BOXES);
            // convert tensors to boxes - Note: Optimized to read-all upfront
            outputs.get(MNETSSD_OUTPUT_LAYER).read(floatOutput, 0, MNETSSD_NUM_BOXES * 7);

            for (int i = 0; i < MNETSSD_NUM_BOXES; i++) {
                float mSSDOutputBoxes[] = new float[MNETSSD_NUM_BOXES * 4];
                float mSSDOutputClasses[] = new float[MNETSSD_NUM_BOXES];
                float mSSDOutputScores[] = new float[MNETSSD_NUM_BOXES];
                mSSDOutputBoxes[(i * 4)] = floatOutput[3 + (7 * i)];
                mSSDOutputBoxes[1 + (i * 4)] = floatOutput[4 + (7 * i)];
                mSSDOutputBoxes[2 + (i * 4)] = floatOutput[5 + (7 * i)];
                mSSDOutputBoxes[3 + (i * 4)] = floatOutput[6 + (7 * i)];
                mSSDOutputClasses[i] = floatOutput[1 + (7 * i)];
                mSSDOutputScores[i] = floatOutput[2 + (7 * i)];

                Box box = mSSDBoxes.get(i);
                box.top = mSSDOutputBoxes[(i * 4)];
                box.left = mSSDOutputBoxes[1 + (4 * i)];
                box.bottom = mSSDOutputBoxes[2 + (4 * i)];
                box.right = mSSDOutputBoxes[3 + (4 * i)];
                box.type_id = (int) mSSDOutputClasses[i];
                box.type_score = mSSDOutputScores[i];
                box.type_name = lookupMsCoco(box.type_id, "???");
                mSSDOutputBoxes = mSSDOutputScores = mSSDOutputClasses = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mSSDBoxes;
    }


    /* Generic functions, for typical image models */

    private Map<String, FloatTensor> inferenceOnBitmap(Bitmap inputBitmap) {
        final Map<String, FloatTensor> outputs;
        try {
            // safety check
            if (mNeuralNetwork == null || mInputTensorReused == null || inputBitmap.getWidth() != getInputTensorWidth() || inputBitmap.getHeight() != getInputTensorHeight()) {
                complain("No NN loaded, or image size different than tensor size");
                return null;
            }

            // [0.3ms] Bitmap to RGBA byte array (size: 300*300*3 (RGBA..))
            mBitmapToFloatHelper.bitmapToBuffer(inputBitmap);

            // [2ms] Pre-processing: Bitmap (300,300,4 ints) -> Float Input Tensor (300,300,3 floats)
            mTimeStat.startInterval();
            final float[] inputFloatsHW3 = mBitmapToFloatHelper.bufferToNormalFloatsBGR();
            if (mBitmapToFloatHelper.isFloatBufferBlack())
                return null;
            mInputTensorReused.write(inputFloatsHW3, 0, inputFloatsHW3.length, 0, 0);
            mTimeStat.stopInterval("i_tensor", 20, false);
            // [31ms on GPU16, 50ms on GPU] execute the inference
            mTimeStat.startInterval();
            outputs = mNeuralNetwork.execute(mInputTensorsMap);
            mTimeStat.stopInterval("nn_exec ", 20, false);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.d(TAG, e.getCause() + "");
            return null;
        }

        return outputs;
    }

    private static NeuralNetwork loadNetworkFromDLCAsset(
            Application application, String assetFileName, NeuralNetwork.Runtime selectedRuntime,
            boolean needsCpuFallback, String... outputLayerNames) {
        try {
            // input stream to read from the assets
            InputStream assetInputStream = application.getAssets().open(assetFileName);

            // create the neural network
            NeuralNetwork network = new SNPE.NeuralNetworkBuilder(application)
                    .setDebugEnabled(false)
                    .setOutputLayers(outputLayerNames)
                    .setModel(assetInputStream, assetInputStream.available())
                    .setPerformanceProfile(NeuralNetwork.PerformanceProfile.HIGH_PERFORMANCE)
                    .setRuntimeOrder(selectedRuntime) // Runtime.DSP, Runtime.GPU_FLOAT16, Runtime.GPU, Runtime.CPU
                    .setCpuFallbackEnabled(needsCpuFallback)
                    .build();

            // close input
            assetInputStream.close();
            // all right, network loaded
            return network;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalStateException | IllegalArgumentException e2) {

            e2.printStackTrace();
            return null;
        }
    }

    public void disposeNeuralNetwork() {
        if (mNeuralNetwork == null)
            return;
        mNeuralNetwork.release();
        mNeuralNetwork = null;
        mInputTensorShapeHWC = null;
        mInputTensorReused = null;
        mInputTensorsMap = null;
    }

    private void complain(String message) {

        // only show the message if on the main thread
        if (Looper.myLooper() == Looper.getMainLooper())
            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    // VERBOSE COCO object map
    private Map<Integer, String> mCocoMap;


    private String lookupMsCoco(int cocoIndex, String fallback) {

        if (mCocoMap == null) {
            mCocoMap = new ArrayMap<>();
            mCocoMap.put(0, "background");
            mCocoMap.put(1, "aeroplane");
            mCocoMap.put(2, "bicycle");
            mCocoMap.put(3, "bird");
            mCocoMap.put(4, "boat");
            mCocoMap.put(5, "bottle");
            mCocoMap.put(6, "bus");
            mCocoMap.put(7, "car");
            mCocoMap.put(8, "cat");
            mCocoMap.put(9, "chair");
            mCocoMap.put(10, "cow");
            mCocoMap.put(11, "diningtable");
            mCocoMap.put(12, "dog");
            mCocoMap.put(13, "horse");
            mCocoMap.put(14, "motorbike");
            mCocoMap.put(15, "person");
            mCocoMap.put(16, "pottedplant");
            mCocoMap.put(17, "sheep");
            mCocoMap.put(18, "sofa");
            mCocoMap.put(19, "train");
            mCocoMap.put(20, "tvmonitor");

        }
        return mCocoMap.containsKey(cocoIndex) ? mCocoMap.get(cocoIndex) : fallback;
    }


}
