package com.ble.healthmonitoringapp.utils;

public interface BleCallback {
    public void scanCallback(String deviceName, String macAddress, int Rssi, int TxPower, String isCoded);
    public void connectionCallback(boolean isConnected, String status);
    public void onCharacteristicChanged(byte[] data);
    public void connectClickCallback(boolean isConnectClick);
    public void drawTrainDot(float x, float y);
    public void bluetoothCallback(boolean isOn);
}
