package com.ble.healthmonitoringapp.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.ble.BleManager;
import com.ble.healthmonitoringapp.ble.BleService;
import com.ble.healthmonitoringapp.dialog.ConnectDeviceDialog;
import com.ble.healthmonitoringapp.dialog.devicesDialog;
import com.ble.healthmonitoringapp.utils.BleData;
import com.ble.healthmonitoringapp.utils.CheckSelfPermission;
import com.ble.healthmonitoringapp.utils.ResolveData;
import com.ble.healthmonitoringapp.utils.RxBus;
import com.ble.healthmonitoringapp.utils.Utilities;
import com.bumptech.glide.Glide;
import com.jstyle.blesdk2025.model.ExtendedBluetoothDevice;
import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DeviceScanActivity extends AppCompatActivity {
    private ImageView iv_src,iv_find_dvs;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    devicesDialog devicesDialog;
    private Disposable subscription;
    private boolean isFirstLocationPermission = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_device);
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        InitUI();
    }

    public void InitUI(){
        iv_src = findViewById(R.id.iv_src);
        iv_find_dvs = findViewById(R.id.iv_find_dvs);
        devicesDialog = new devicesDialog();
        devicesDialog.showDialog(DeviceScanActivity.this);
        Glide.with(this).load(R.raw.searching).into(iv_src).onStart();
        Glide.with(this).load(R.raw.finding_device).into(iv_find_dvs).onStart();
        subscription = RxBus.getInstance().toObservable(BleData.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<BleData>() {
            @Override
            public void accept(BleData bleData) throws Exception {
                String action = bleData.getAction();
                if (action.equals(BleService.ACTION_GATT_onDescriptorWrite)) {
                    scanLeDevice(false);
                    Utilities.dissMissDialog();
                    ConnectDeviceDialog connectDeviceDialog = new ConnectDeviceDialog();
                    connectDeviceDialog.showDialog(DeviceScanActivity.this,Utilities.DeviceName);
                } else if (action.equals(BleService.ACTION_GATT_DISCONNECTED)) {
                    Utilities.dissMissDialog();
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(checkselfPermission()) {
            scanLeDevice(true);
        }
    }
    private boolean checkselfPermission(){
        if(CheckSelfPermission.isBluetooth12Permission(DeviceScanActivity.this)){
        if (CheckSelfPermission.isBluetoothOn(DeviceScanActivity.this)){
            if (CheckSelfPermission.isLocationOn(DeviceScanActivity.this)){
                if(checkLocationPermission()){
                   if(CheckSelfPermission.checkStoragePermission(this)){
                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                           if (Environment.isExternalStorageManager()) {
                               return true;
                           } else {
                               Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                               intent.setData(Uri.parse("package:" + getPackageName()));
                               startActivityForResult(intent, 0);
                           }
                       }else {
                           return true;
                       }
                   }
                }
            }
        }
        }
        return false;
    }

    private boolean checkLocationPermission(){
        if (isFirstLocationPermission){
            isFirstLocationPermission = false;
            return CheckSelfPermission.checkLocationPermission(DeviceScanActivity.this);
        }else {
            return CheckSelfPermission.checkLocationPermissionRetional(DeviceScanActivity.this);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        extendedBluetoothDevices.clear();
        devicesDialog.devicesAdapter.clear();
    }


    @SuppressLint("MissingPermission")
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                    invalidateOptionsMenu();
                }
            }, 12000);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //	mBluetoothAdapter.startLeScan(serviceUuids, mLeScanCallback);
            mScanning = true;
        } else {
            if (!mScanning) return;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
        }
        invalidateOptionsMenu();
    }

    int filterRssi = -100;
    private ArrayList<ExtendedBluetoothDevice> extendedBluetoothDevices = new ArrayList<>();

    private ExtendedBluetoothDevice findDevice(final BluetoothDevice device) {
        for (final ExtendedBluetoothDevice mDevice : extendedBluetoothDevices) {
            if (mDevice.matches(device)) return mDevice;
        }
        return null;
    }

    public void addDevice(BluetoothDevice device, String name, int rssi) {
        ExtendedBluetoothDevice bluetoothDevice = findDevice(device);
        if (bluetoothDevice == null) {
            extendedBluetoothDevices.add(new ExtendedBluetoothDevice(device, name, rssi));
        } else {
            bluetoothDevice.rssi = rssi;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    @SuppressLint("MissingPermission") String deviceName = device.getName();
                    if (TextUtils.isEmpty(deviceName)) {
                        deviceName = ResolveData.decodeDeviceName(scanRecord);
                    }
                    if (TextUtils.isEmpty(deviceName))
                    deviceName = "unknown device";
                    if(deviceName.toLowerCase().contains("J2025E".toLowerCase())){
                        addDevice(device, deviceName, rssi);
                        devicesDialog.show(extendedBluetoothDevices);
                        if (rssi > filterRssi) {
                        devicesDialog.devicesAdapter.addDevice(device, deviceName, rssi);
                        devicesDialog.devicesAdapter.notifyDataSetChanged();
                    }
                 }
                }
            });
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
        if (BleManager.getInstance().isConnected()) BleManager.getInstance().disconnectDevice();
    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
           // Log.i(TAG, "unSubscribe: ");
        }
    }

}