package com.qc.dashcam.CommonUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class Util {

    public static void showProgress(Context mContext, ProgressDialog progressDialog) {
        progressDialog.setMessage("Loading model..");
        progressDialog.show();

    }

    public static void stopProgress(Context mContext, ProgressDialog progressDialog) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }

    /**
     * Method to check whether camera permission is granted or not
     *
     * @return
     */
    public static boolean hasPermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContext.checkSelfPermission(Constants.PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

}
