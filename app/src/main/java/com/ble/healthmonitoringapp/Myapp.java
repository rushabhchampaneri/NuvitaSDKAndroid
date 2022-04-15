package com.ble.healthmonitoringapp;

import android.app.Application;
import android.content.Context;

import com.ble.healthmonitoringapp.ble.BleManager;
import com.bugfender.sdk.Bugfender;


public class Myapp extends Application {

    public static Context context;
    public static Myapp getInStance(){
        return new Myapp();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
        BleManager.init(this);
        Bugfender.init(this, "Er2ZKW4kHHLY4KPrrWwrHWlwvCY23ghP", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        Bugfender.enableUIEventLogging(this);
        Bugfender.enableLogcatLogging();
    }
}
