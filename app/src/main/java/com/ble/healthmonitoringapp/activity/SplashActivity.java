package com.ble.healthmonitoringapp.activity;

import static com.ble.healthmonitoringapp.utils.AppConstants.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.utils.FireBaseKey;
import com.ble.healthmonitoringapp.utils.LocaleHelper;
import com.ble.healthmonitoringapp.utils.Utilities;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class SplashActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        LocaleHelper.setLocale(this, "en");
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