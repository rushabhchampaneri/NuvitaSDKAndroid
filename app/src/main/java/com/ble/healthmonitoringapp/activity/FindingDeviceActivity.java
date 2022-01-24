package com.ble.healthmonitoringapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.dialog.devicesDialog;

public class FindingDeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_device);

        goToNextActivity();
    }

    private void goToNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                devicesDialog devicesDialog = new devicesDialog();
                devicesDialog.showDialog(FindingDeviceActivity.this);
            }
        }, 3000);
    }
}