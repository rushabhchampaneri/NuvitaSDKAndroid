package com.ble.healthmonitoringapp.model;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Date;

public class ConnectDeviceModel implements Serializable {
    public String deviceName;
    public String macAddress;
    public int rssi;
    public int txPower;
    public String isCoded;
    public Date date;
    public boolean isConnected;
    public String battery_level = "N/A";
    public long battery_level_time;

    public ConnectDeviceModel(String deviceName, String macAddress, int rssi, int txPower, String isCoded, Date date, boolean isConnected) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.rssi = rssi;
        this.txPower = txPower;
        this.isCoded = isCoded;
        this.date = date;
        this.isConnected=isConnected;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }


    public String getIsCoded() {
        return isCoded;
    }

    public void setIsCoded(String isCoded) {
        this.isCoded = isCoded;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(String battery_level) {
        this.battery_level = battery_level;
    }

    public long getBattery_level_time() {
        return battery_level_time;
    }

    public void setBattery_level_time(long battery_level_time) {
        this.battery_level_time = battery_level_time;
    }
}
