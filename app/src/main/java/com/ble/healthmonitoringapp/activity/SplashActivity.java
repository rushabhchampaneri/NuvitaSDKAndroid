package com.ble.healthmonitoringapp.activity;

import static com.ble.healthmonitoringapp.activity.GlobalApplication.buildGoogleApiClient;
import static com.ble.healthmonitoringapp.ble.BleDeviceActor.reconnectDevice;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.ble.BleDeviceActor;
import com.ble.healthmonitoringapp.utils.CheckSelfPermission;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        goToNextActivity();
    }

    private void goToNextActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mIntent = new Intent(SplashActivity.this, FindingDeviceActivity.class);
                startActivity(mIntent);
                finish();
            }
        },3000);
    }
    @Override
    protected void onResume() {
        super.onResume();



    }



}