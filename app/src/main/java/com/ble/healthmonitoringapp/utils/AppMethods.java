package com.ble.healthmonitoringapp.utils;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AppMethods {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static ProgressDialog pDialog;
    public static AlertDialog alertDialog;
    public static FileOutputStream outputStream = null;

    public static double calculateAccuracy(float x, float y) {
        double accuracy = Math.pow((Math.pow(x, 2) + Math.pow(y, 2)), 0.5);
        return accuracy * 100;
    }


    public static byte[] convertStringToByte(String text) {
        try {
            return text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String convertByteToString(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static double getDistance(int rssi, int txPower) {
        double distance = Math.pow(10d, ((double) txPower - rssi) / (10 * 2));

            return distance;
    }


    public static void showProgressDialog(Context context, String msg) {
        hideProgressDialog(context);
        try {
            if (context != null) {
                pDialog = new ProgressDialog(context);
                pDialog.setMessage(msg);
                pDialog.setCancelable(false);
                pDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideProgressDialog(Context context) {
        try {
            if (context != null && pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAlertDialog(final Context context, final String msg, String title) {
        hideAlertDialog();
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage(msg);
                alertDialogBuilder.setTitle(title);
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });

                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.setCancelable(true);
                alertDialog.setCanceledOnTouchOutside(false);
            }
        });
    }

    public static void hideAlertDialog() {
        try {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }



    public static void writeDataTostrem(Context context, String data) {

        try {
            if (outputStream != null) {
                outputStream.write(convertStringToByte("\n" + getCurrentDate() + " " + data));
                Log.d(AppConstants.TAG, "writeDataTostrem: " + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentDate() {
        long statictime = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        return formatter.format(statictime);
    }

    public static String getLogDate(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd, yyyy");
        return formatter.format(time);
    }

    public static String getLogTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
        return formatter.format(time);
    }

    public static String getDateFormateFormLong(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return formatter.format(time);
    }

    public static void setLocale(Activity context) {
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }


    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static int getStrength(int RSSI){
        int Strength=0;
        int input = RSSI;
        int Min = -100;
        int Max = -25;

        Strength = (input+100)*100/(75);

        return Strength;
    }

}
