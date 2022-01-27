package com.ble.healthmonitoringapp.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.utils.AppConstants;
import com.ble.healthmonitoringapp.utils.AppMethods;
import com.ble.healthmonitoringapp.utils.CheckSelfPermission;

import java.util.UUID;

public class BleCharacteristic {
    public static boolean WriteCharacteristic(Context context, byte[] byteValue) {
        BluetoothGatt mBluetoothGatt = BleDeviceActor.getmBluetoothGatt();
        if (!canReadWrite(context, mBluetoothGatt)) {
            return false;
        }
        BluetoothGattService service = mBluetoothGatt
                .getService(UUID.fromString(AppConstants.UART_SERVICE_UUID));
        if (service != null) {
            BluetoothGattCharacteristic characteristic =
                    service.getCharacteristic(UUID.fromString(AppConstants.WRITE_CHAR_UUID));
            if (characteristic != null) {
                characteristic.setValue(byteValue);
                boolean isWrite = mBluetoothGatt.writeCharacteristic(characteristic);
                Log.d(AppConstants.TAG, "writeCharacteristic: " + isWrite);
                return isWrite;
            } else {
                AppMethods.setAlertDialog(context, AppConstants.WRITE_CHAR_UUID + " characteristic not found.", "");
            }
        } else {
            AppMethods.setAlertDialog(context, AppConstants.UART_SERVICE_UUID + " service not found.", "");
        }
        return false;
    }

    public static void enableNotifychar(Context context) {
        BluetoothGatt mBluetoothGatt = BleDeviceActor.getmBluetoothGatt();
        if (!canReadWrite(context, mBluetoothGatt)) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt
                .getService(UUID.fromString(AppConstants.UART_SERVICE_UUID));
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service
                    .getCharacteristic(UUID.fromString(AppConstants.NOTIFY_CHAR_UUID));
            if (characteristic != null) {
                BluetoothGattDescriptor descriptor =
                        characteristic.getDescriptor(UUID.fromString(AppConstants.DESC_UUID));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                boolean isWrite = mBluetoothGatt.writeDescriptor(descriptor);
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                Log.d("ble==> ", "enableNotifyWifiStatusLevel: " + isWrite);
            } else {
                AppMethods.setAlertDialog(context, AppConstants.NOTIFY_CHAR_UUID + " characteristic not found.", "");
                AppMethods.hideProgressDialog(context);
            }
        } else {
            AppMethods.setAlertDialog(context, AppConstants.UART_SERVICE_UUID + " service not found.", "");
            AppMethods.hideProgressDialog(context);
        }
    }

    public static boolean canReadWrite(Context context, BluetoothGatt mBluetoothGatt) {
        if (!CheckSelfPermission.isBluetoothOn(context)) {
            AppMethods.setAlertDialog(context, context.getString(R.string.enable_bluetooth), "");
            return false;
        } else if (!BleDeviceActor.isIsConnected()) {
            AppMethods.setAlertDialog(context, context.getString(R.string.device_disconnected), "");
            return false;
        } else if (mBluetoothGatt == null) {
            return false;
        } else {
            return true;
        }
    }
}
