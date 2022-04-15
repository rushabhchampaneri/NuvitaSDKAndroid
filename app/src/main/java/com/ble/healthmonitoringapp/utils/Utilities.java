package com.ble.healthmonitoringapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.ble.healthmonitoringapp.R;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utilities {
    public static Context mContext;
    public static boolean FirstFound = false;
    private static final String PREFERENCE = "healthmonitoringapp";
    public static ProgressDialog progressDialog;
    public static String MacAddress="";
    public static String DeviceName="";
    public static String LastReadingTime="LastReadingTime";
    public static String LastUploadTime="LastUploadTime";


    public static void setValue(@NonNull Context context, String key, String value) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor prefsPrivateEditor = preferences.edit();
            prefsPrivateEditor.putString(key, value);
            prefsPrivateEditor.commit();
        } catch (Exception e) {

        }
    }

    public static String getValue(@NonNull Context context, String key, String defaultValue) {
        String result = "";
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            result = preferences.getString(key, defaultValue);
        } catch (Exception e) {

        }
        return result;
    }


    public static void setRequestPreference(Context context, String name, String value) {
        mContext = context;
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE, 0);
        SharedPreferences.Editor editor = settings.edit();
        // editor.clear();
        editor.putString(name, value);
        editor.apply();
    }

    public static String getRequestPreferences(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE, 0);
        return settings.getString(name, "");
    }

    public static void setRqtPreference(Context context, boolean name, String value) {
        mContext = context;
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE, 0);
        SharedPreferences.Editor editor = settings.edit();
        // editor.clear();
        editor.putString(String.valueOf(name), value);
        editor.apply();
    }

    public static String getRqtPreferences(Context context, boolean name) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE, 0);
        return settings.getString(String.valueOf(name), "");
    }


    public static void removepreference(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE, 0);
        settings.edit().remove(name).apply();
    }

    public static void clearPreference(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE, 0);
        settings.edit().clear().apply();
    }

    public static void hideKeyBoard(EditText edt, Context ct) {
        InputMethodManager imm = (InputMethodManager) ct.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void ShowToastMessage(Context ctactivity, String message) {
        Toast toast = Toast.makeText(ctactivity, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void ShowToastMessage(Context context, int message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd | HH:mm:ss", Locale.getDefault());
        String datetime = dateformat.format(c.getTime());
        System.out.println(datetime);

        return  datetime;

    }
    public static String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String datetime = dateformat.format(c.getTime());
        System.out.println(datetime);
        return  datetime;

    }
    public static  HashMap<String, Object> getTimeHashmap(Map<String, Object> map){
        HashMap<String, Object> timeMap = new HashMap<>();
        timeMap.put(getTime(),map);
       return timeMap;
    }
    public static  HashMap<String, Object> getTimeHashmap(Map<String, Object> map,String time){
        HashMap<String, Object> timeMap = new HashMap<>();
        timeMap.put(time,map);
        return timeMap;
    }
    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
    public static String getFIleCreateDate() {
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
    public  static int getValueInt(String s){
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public  static float getValueFloat(String s){
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public static String getDeciveDate(String time){
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd-MM-yyyy");
        String str = null;
        try {
            Date date = parseFormat.parse(time);
            str = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
    public static String getDeciveTime(String time){
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm:ss");
        String str = null;
        try {
            Date date = parseFormat.parse(time);
            str = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }
    public static String getTime(String time) {
        SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
        String str = null;
        try {
            Date date = parseFormat.parse(time);
            str = displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static ArrayList<String> getStringList(Context context, String key) {
        ArrayList<String> value = new ArrayList<>();
        try {
            SharedPreferences preferences =
                    context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE |
                            Context.MODE_MULTI_PROCESS);

            if (preferences != null) {
                try {
                    value = (ArrayList<String>) ObjectSerializer.deserialize(preferences.getString(key, ObjectSerializer.serialize(new ArrayList<String>())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        }
        return value;
    }

    public static boolean setStringList(Context context, String key, ArrayList<String> value) {
        SharedPreferences preferences =
                context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE |
                        Context.MODE_MULTI_PROCESS);
        if (preferences != null && !TextUtils.isEmpty(key)) {
            SharedPreferences.Editor editor = preferences.edit();
            try {
                editor.putString(key, ObjectSerializer.serialize(value));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return editor.commit();
        }
        return false;
    }
    public static void showConnectDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.connectting));
        progressDialog.setCancelable(false);
        if (!progressDialog.isShowing()) progressDialog.show();
    }
    public static void showProgress(Context context,String mes) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(mes);
        progressDialog.setCancelable(false);
        if (!progressDialog.isShowing()) progressDialog.show();
    }
    public static void dissMissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }
}
