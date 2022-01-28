package com.ble.healthmonitoringapp;

import android.app.Application;

import com.ble.healthmonitoringapp.ble.BleManager;


public class Myapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BleManager.init(this);
    }
}
