package com.qc.dashcam.CommonUtil;

import android.util.Log;

public class Logger {

    private static boolean isLogEnabled = true;

    /**
     * Method to show Debug Logs
     *
     * @param tag
     * @param msg
     */

    public static void d(String tag, String msg) {
        if (isLogEnabled)
            Log.d(tag, msg);

    }


    /**
     * Method to show warning Logs
     *
     * @param tag
     * @param msg
     */
    public static void w(String tag, String msg) {
        if (isLogEnabled)
            Log.w(tag, msg);

    }

    /**
     * Method to show Verbose Logs
     *
     * @param tag
     * @param msg
     */
    public static void v(String tag, String msg) {
        if (isLogEnabled)
            Log.v(tag, msg);

    }

    /**
     * Method to show Error Logs
     *
     * @param tag
     * @param msg
     * @param t
     */
    public static void e(String tag, String msg, Throwable t) {
        if (isLogEnabled)
            Log.e(tag, msg, t);

    }

    /**
     * Method to show Error Logs
     *
     * @param tag
     * @param msg
     */

    public static void e(String tag, String msg) {
        if (isLogEnabled)
            Log.e(tag, msg);

    }
}