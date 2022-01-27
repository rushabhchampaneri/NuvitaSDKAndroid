package com.ble.healthmonitoringapp.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.activity.MainActivity;
import com.ble.healthmonitoringapp.utils.AppConstants;
import com.ble.healthmonitoringapp.utils.AppMethods;
import com.ble.healthmonitoringapp.utils.BleCallback;
import com.ble.healthmonitoringapp.utils.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleDeviceActor implements Runnable {
    private static Context mContext;
    private static BluetoothDevice mBluetoothDevice;
    private static BluetoothGatt mBluetoothGatt;
    private static Thread thread;
    public static boolean isConnected = false;
    public static int MTU = 20;
    private BluetoothLeScanner btScanner;
    private BluetoothManager btManager;
    private static BluetoothAdapter btAdapter;
    public static BleCallback bleCallback;
    List<UUID> serviceUUIDsList = new ArrayList<>();

    public static BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    public static boolean isIsConnected() {
        return isConnected;
    }

    public BleDeviceActor(Context mContext) {
        this.mContext = mContext;
    }

    public void setCallback(BleCallback bleCallback) {
        this.bleCallback = bleCallback;
    }

    public static void connectToDevice() {
        Log.d(AppConstants.TAG,"connectToDevice");
        String macAddress = Utilities.getRequestPreferences(mContext,AppConstants.CONNECTED_MAC_ADDRESS_PREF);
        if (macAddress.equals("")){
            return;
        }
        BluetoothManager btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter btAdapter = btManager.getAdapter();
        if (!btAdapter.isEnabled()){
            return;
        }
        mBluetoothDevice = btAdapter.getRemoteDevice(macAddress);
        if (mBluetoothDevice == null || mContext == null) {
            return;
        }
        try {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_LE_1M);
            }else {
                mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
            }
        } else {
            mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mBluetoothGatt != null) {
                mBluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnectDevice() {
        try {
            if (mBluetoothGatt != null && isConnected) {
                mBluetoothGatt.disconnect();
                isConnected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopThread();
    }

    public static BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(AppConstants.TAG, "onConnectionStateChange: " + newState+", status:"+status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false;
                if (status == 0 || status == 19) {
                    bleCallback.connectionCallback(false, "");
                }else if (status == 8){
//                    bleCallback.connectionCallback(false, " Connection error: Device went out of range");
                    bleCallback.connectionCallback(false, "");
                }else if (status == 22){
                    bleCallback.connectionCallback(false, " Connection error: Issue with bond");
                }else {
                    bleCallback.connectionCallback(false, " Connection error status:"+status);
                }
                reconnectDevice();
            } else {
                isConnected = false;
                bleCallback.connectionCallback(false, " Connection error status:"+status);
                reconnectDevice();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(AppConstants.TAG, "onServicesDiscovered");
            if (status == gatt.GATT_SUCCESS) {
                if (gatt != null) {
                    Log.d(AppConstants.TAG, "onServicesDiscovered: success");
                    mBluetoothGatt = gatt;
                    isConnected = true;
                    BleCharacteristic.enableNotifychar(mContext);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(AppConstants.TAG, "onCharacteristicRead: " + Arrays.toString(characteristic.getValue()));
            if (characteristic != null) {
                byte[] data = characteristic.getValue();
                if (data != null) {
                    switch (characteristic.getUuid().toString().toLowerCase()) {
                    }
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(AppConstants.TAG, "onCharacteristicWrite: " + Arrays.toString(characteristic.getValue()));
            if (characteristic != null && characteristic.getValue() != null && characteristic.getValue().length > 0) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    AppMethods.writeDataTostrem(mContext,"Sent " + AppMethods.convertByteToString(characteristic.getValue()));
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(AppConstants.TAG, "onCharacteristicChanged: " + Arrays.toString(characteristic.getValue()));
            if (characteristic != null) {
                byte[] data = characteristic.getValue();
                if (data != null) {
                    switch (characteristic.getUuid().toString().toUpperCase()) {
                        case AppConstants.NOTIFY_CHAR_UUID:
                            String dataString = AppMethods.convertByteToString(data);
                            AppMethods.writeDataTostrem(mContext,"Received " + dataString);
                            bleCallback.onCharacteristicChanged(data);
                            break;
                    }
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(AppConstants.TAG, "onDescriptorWrite: success " + descriptor.getCharacteristic().getUuid().toString() + "   value: " + descriptor.getValue()[0]);
                gatt.requestMtu(255);
            } else {
                Log.e(AppConstants.TAG, "onDescriptorWrite: false status: " + status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == gatt.GATT_SUCCESS) {
                Log.d(AppConstants.TAG, "onMtuChanged: " + mtu);
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O /*&& btAdapter.isLe2MPhySupported()*/) {
                        Log.d(AppConstants.TAG, "setPreferredPhy");
                        gatt.setPreferredPhy(BluetoothDevice.PHY_LE_CODED_MASK,BluetoothDevice.PHY_LE_CODED_MASK,BluetoothDevice.PHY_OPTION_S8);
                    }
                    recoonect = 0;
                    bleCallback.connectionCallback(true,"");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            if (status == gatt.GATT_SUCCESS) {
                AppMethods.writeDataTostrem(mContext,"Set Phy status: success");
            }else {
                AppMethods.writeDataTostrem(mContext,"Set Phy status: "+status);
            }
            Log.d(AppConstants.TAG, "onPhyUpdate: " + txPhy+", "+rxPhy);

        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.d(AppConstants.TAG, "onPhyUpdate read: " + txPhy+", "+rxPhy);
        }
    };

    public static int recoonect = 0;
    public static void reconnectDevice(){
            if (recoonect==0){
                recoonect = recoonect+1;
                connectToDevice();
            }else {
                recoonect = 0;
                AppMethods.setAlertDialog(mContext, mContext.getString(R.string.device_disconnected),"");
            }

    }

    public void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    public static void stopThread() {
        if (thread != null) {
            final Thread tempThread = thread;
            thread = null;
            tempThread.interrupt();
        }
    }

    public static void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }

    private static void broadcastUpdate(String action, int data) {
        final Intent intent = new Intent(action);
        intent.putExtra("data", data);
        mContext.sendBroadcast(intent);
    }

    public void startScan() {
        stopThread();
        startThread();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void run() {
        stopScan();
        btManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btScanner = btAdapter.getBluetoothLeScanner();
            ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
            scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            scanSettingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

            List<ScanFilter> filters = new ArrayList<ScanFilter>();

            if (btScanner != null) {
                btScanner.startScan(filters, scanSettingsBuilder.build(), ScanCallback);
            } else {
                AppMethods.hideProgressDialog(mContext);
                AppMethods.setAlertDialog(mContext, mContext.getString(R.string.scan_not_supported), "");
                return;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScan() {
        try {
            if (btAdapter.isEnabled()) {
                if (btScanner != null)
                    btScanner.stopScan(ScanCallback);
            }
        } catch (Exception e) {

        }
    }

    private android.bluetooth.le.ScanCallback ScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice scannedDevice = result.getDevice();
            if (scannedDevice != null) {
                if (result.getScanRecord() != null && result.getScanRecord().getDeviceName() != null) {
                    String deviceName = result.getScanRecord().getDeviceName().trim();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.d(AppConstants.TAG, "phy: " + result.getScanRecord().getDeviceName() + " primary:"+result.getPrimaryPhy()+" secondary:"+result.getSecondaryPhy());
                    }
                    String DeviceType="";
                    BluetoothClass bluetoothClass = result.getDevice().getBluetoothClass();
                    bleCallback.scanCallback(deviceName, scannedDevice.getAddress(), result.getRssi(),result.getTxPower(),isCoded(result));

                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            AppMethods.hideProgressDialog(mContext);
            AppMethods.setAlertDialog(mContext, mContext.getString(R.string.scan_fail), "");
        }
    };
    private List<UUID> getServiceUUIDsList(ScanResult scanResult)
    {
        List<ParcelUuid> parcelUuids = scanResult.getScanRecord().getServiceUuids();

        List<UUID> serviceList = new ArrayList<>();

        for (int i = 0; i < parcelUuids.size(); i++)
        {
            UUID serviceUUID = parcelUuids.get(i).getUuid();

            if (!serviceList.contains(serviceUUID))
                serviceList.add(serviceUUID);
                Log.e("Service UUID:==>",""+serviceUUID.toString());
        }

        return serviceList;
    }
    private String isCoded(ScanResult result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (result.getPrimaryPhy() == BluetoothDevice.PHY_LE_CODED || result.getPrimaryPhy() == BluetoothDevice.PHY_LE_CODED_MASK || result.getSecondaryPhy() == BluetoothDevice.PHY_LE_CODED || result.getSecondaryPhy() == BluetoothDevice.PHY_LE_CODED_MASK) {
                return "Long";
            }
        }
        return "Short";
    }

}
