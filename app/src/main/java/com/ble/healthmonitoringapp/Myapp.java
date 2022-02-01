package com.ble.healthmonitoringapp;

import android.app.Application;

import com.ble.healthmonitoringapp.ble.BleManager;
import com.bugfender.sdk.Bugfender;


public class Myapp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BleManager.init(this);
        Bugfender.init(this, "Er2ZKW4kHHLY4KPrrWwrHWlwvCY23ghP", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        Bugfender.enableUIEventLogging(this);
        Bugfender.enableLogcatLogging();
    }
}
