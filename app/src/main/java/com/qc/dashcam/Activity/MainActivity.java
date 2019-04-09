package com.qc.dashcam.Activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.qc.dashcam.CommonUtil.Constants;
import com.qc.dashcam.CommonUtil.Logger;
import com.qc.dashcam.CommonUtil.Util;
import com.qc.dashcam.Fragment.CameraPreviewFragment;
import com.qc.dashcam.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Method to navigate to CameraPreviewFragment
     */

    private void goToCameraPreviewFragment() {
        if (Util.hasPermission(MainActivity.this)) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();// = getFragmentManager().beginTransaction();
            transaction.add(R.id.main_content, CameraPreviewFragment.create());
            transaction.commit();
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        goToCameraPreviewFragment();

    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToCameraPreviewFragment();
            }
        }
    }

    /**
     * Method to request Camera permission
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Constants.PERMISSION_CAMERA)) {
                Toast.makeText(this,
                        getString(R.string.toast_camera_permission), Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{Constants.PERMISSION_CAMERA}, Constants.PERMISSIONS_REQUEST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop()");
    }

}