package com.ble.healthmonitoringapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.ble.healthmonitoringapp.R;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

/**
 * Created by Administrator on 2018/9/5.
 */

public class PermissionsUtil {
    private static final String TAG = "PermissionsUtil";

    public static void requestPermissions(final AppCompatActivity activity, final PermissionListener  permissionListener, final String... permissions) {
        if(activity.isFinishing())return;
        RxPermissions rxPermission = new RxPermissions(activity);
        rxPermission.requestEach(permissions)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            if(permissionListener!=null)
                            permissionListener.granted(permission.name);
                            Log.d(TAG, permission.name + " is granted.");
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            showNeedPermission(activity,permissionListener,permission.name);
                            Log.d(TAG, permission.name + " is denied. More info should be provided.");
                        } else {
                            showToEnable(activity);
                        }
                    }
                });

    }

    public static void showNeedPermission(final AppCompatActivity activity, final PermissionListener  permissionListener, final String permissionName) {
        if(activity.isFinishing())return;
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle( R.string.permission_requierd)
                .setMessage(String.format(activity.getString(R.string.permisson_neverask),permissionName))
                .setPositiveButton(activity.getString(R.string.require_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(activity,permissionListener,permissionName);
                    }
                }).setNegativeButton(activity.getString(R.string.require_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(permissionListener!=null) permissionListener.disallow(permissionName);
                    }
                }).create();
        alertDialog.show();
    }

    public static void showToEnable(final AppCompatActivity context) {
        if(context.isFinishing())return;
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle(context.getString(R.string.Enable_Access_title))
                .setMessage(context.getString(R.string.access_content))
                .setPositiveButton(context.getString(R.string.access_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", context.getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        context.startActivity(intent);
                    }
                }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        alertDialog.show();
    }
    public interface PermissionListener{
        public void granted(String name);
        public void NeverAskAgain();
        public void disallow(String name);
    }

    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

}
