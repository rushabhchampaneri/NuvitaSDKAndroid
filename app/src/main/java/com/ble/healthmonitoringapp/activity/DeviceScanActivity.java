package com.ble.healthmonitoringapp.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.dialog.devicesDialog;
import com.ble.healthmonitoringapp.utils.PermissionsUtil;
import com.ble.healthmonitoringapp.utils.ResolveData;
import com.bumptech.glide.Glide;
import com.jstyle.blesdk2025.model.ExtendedBluetoothDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceScanActivity extends AppCompatActivity implements PermissionsUtil.PermissionListener {
    private ImageView iv_src,iv_find_dvs;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    devicesDialog devicesDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_device);
        iv_src = findViewById(R.id.iv_src);
        iv_find_dvs = findViewById(R.id.iv_find_dvs);
        devicesDialog = new devicesDialog();
        devicesDialog.showDialog(DeviceScanActivity.this);
        Glide.with(this).load(R.raw.searching).into(iv_src).onStart();
        Glide.with(this).load(R.raw.finding_device).into(iv_find_dvs).onStart();
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
        PermissionsUtil.requestPermissions(this, this, Manifest.permission.ACCESS_FINE_LOCATION);

    }

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                PermissionsUtil.requestPermissions(this, this, Manifest.permission.ACCESS_FINE_LOCATION);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            case R.id.menu_filter:
                //showFilterDialog();
                break;
        }
        return true;
    }*/


    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device. If Bluetooth is not
        // currently enabled,
        // fire an intent to display a dialog asking the user to grant
        // permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
                if (device == null)
                    return;
                String name = mLeDeviceListAdapter.getName(position);
                if (mScanning) {
                    scanLeDevice(false);
                }
                final Intent intent = new Intent(DeviceScanActivity.this, MainActivity.class);
                intent.putExtra("address", device.getAddress());
                intent.putExtra("name", name);
                startActivity(intent);

            }
        });*/
       /* Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        List<ExtendedBluetoothDevice> list = new ArrayList<>();
        for (BluetoothDevice device : devices) {
            list.add(new ExtendedBluetoothDevice(device));
        }
        devicesDialog.devicesAdapter.addBondDevice(list);*/
        scanLeDevice(true);
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


    private void scanLeDevice(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
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

    @Override
    public void granted(String name) {
        if (Manifest.permission.ACCESS_FINE_LOCATION.equals(name)) {
            if (devicesDialog.devicesAdapter != null) devicesDialog.devicesAdapter.clear();
            if (extendedBluetoothDevices != null) extendedBluetoothDevices.clear();
            scanLeDevice(true);
        }
    }

    @Override
    public void NeverAskAgain() {

    }

    @Override
    public void disallow(String name) {

    }


    // Device scan callback.

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String deviceName = device.getName();
                    if (TextUtils.isEmpty(deviceName)) {
                        deviceName = ResolveData.decodeDeviceName(scanRecord);
                    }
                    if (TextUtils.isEmpty(deviceName))
                        deviceName = "unknown device";
                    addDevice(device, deviceName, rssi);
                    devicesDialog.show(extendedBluetoothDevices);

                    if (rssi > filterRssi) {
                        devicesDialog.devicesAdapter.addDevice(device, deviceName, rssi);
                        devicesDialog.devicesAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

}