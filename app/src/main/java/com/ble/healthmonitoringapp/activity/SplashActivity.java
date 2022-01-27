package com.ble.healthmonitoringapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.ble.healthmonitoringapp.R;


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
                Intent mIntent = new Intent(SplashActivity.this, DeviceScanActivity.class);
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