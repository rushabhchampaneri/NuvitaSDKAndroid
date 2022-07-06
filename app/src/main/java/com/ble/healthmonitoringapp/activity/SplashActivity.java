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
     //   FirebaseFirestore   db=FirebaseFirestore.getInstance();

       /* HashMap<String, Object> FolderMap = new HashMap<>();
        HashMap<String, Object> DateMap = new HashMap<>();
        HashMap<String, Object> DateMa1 = new HashMap<>();
        HashMap<String, Object> timeMap = new HashMap<>();
        HashMap<String, Object> timeMa1 = new HashMap<>();
        Map<String, Object> activityDetail = new HashMap<>();
        activityDetail.put(FireBaseKey.Step, 1323);
        activityDetail.put(FireBaseKey.Kcal, 2.4);
        activityDetail.put(FireBaseKey.Distance, 1.5);
        Map<String, Object> HeartMap = new HashMap<>();
        HeartMap.put(FireBaseKey.HeartValue, 1323);
        HeartMap.put(FireBaseKey.Avg, 24);
        HeartMap.put(FireBaseKey.Max, 29);
        HeartMap.put(FireBaseKey.Min, 15);
        timeMap.put(Utilities.getTime(),activityDetail);
        DateMap.put(Utilities.getCurrentDate(),timeMap);
        FolderMap.put(FireBaseKey.FIREBASE_ActivityDetail,DateMap);
        timeMa1.put(Utilities.getTime(),HeartMap);
       // DateMa1.put(Utilities.getCurrentDate(),timeMa1);
       // FolderMap.put(FireBaseKey.HeartValue,DateMa1);
        db.collection(FireBaseKey.FIREBASE_COLLECTION_NAME).
                document("D8:14:B9:EC:4B:44")
                .collection(FireBaseKey.FIREBASE_ActivityDetail)
                .document(Utilities.getCurrentDate())
                .set(timeMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
        db.collection(FireBaseKey.FIREBASE_COLLECTION_NAME).
                document("D8:14:B9:EC:4B:44")
                .collection(FireBaseKey.FIREBASE_HeartRate)
                .document(Utilities.getCurrentDate())
                .set(timeMa1, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });*/
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