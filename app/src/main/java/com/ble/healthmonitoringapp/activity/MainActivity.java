package com.ble.healthmonitoringapp.activity;

import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.ble.BleManager;
import com.ble.healthmonitoringapp.ble.BleService;
import com.ble.healthmonitoringapp.databinding.ActivityMainBinding;
import com.ble.healthmonitoringapp.model.EcgHistoryData;
import com.ble.healthmonitoringapp.model.TempModel;
import com.ble.healthmonitoringapp.utils.AppMethods;
import com.ble.healthmonitoringapp.utils.BleData;
import com.ble.healthmonitoringapp.utils.CheckSelfPermission;
import com.ble.healthmonitoringapp.utils.FireBaseKey;
import com.ble.healthmonitoringapp.model.HeartRateModel;
import com.ble.healthmonitoringapp.model.HrvModel;
import com.ble.healthmonitoringapp.utils.LocaleHelper;
import com.ble.healthmonitoringapp.utils.RxBus;
import com.ble.healthmonitoringapp.model.StepDetailData;
import com.ble.healthmonitoringapp.utils.Utilities;
import com.ble.healthmonitoringapp.model.ValuesModel;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.jstyle.blesdk2025.Util.BleSDK;
import com.jstyle.blesdk2025.constant.BleConst;
import com.jstyle.blesdk2025.constant.DeviceKey;
import com.jstyle.blesdk2025.model.AutoMode;
import com.jstyle.blesdk2025.model.MyAutomaticHRMonitoring;
import com.jstyle.blesdk2025.model.MyDeviceTime;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }
    ActivityMainBinding binding;
    private Disposable subscription;
    byte ModeStart = 0;
  //  int ModeStart = 2;

    byte ModeContinue = 2;
    byte ModeDelete = (byte)0x99;
    private String date;
    List<Map<String, String>> list = new ArrayList<>();
    private List<Map<String, String>> listDetail = new ArrayList<>();
    private List<Map<String, String>> listHRV = new ArrayList<>();
    private List<Map<String, String>> listSleep = new ArrayList<>();
    private List<Map<String, String>> listHeart = new ArrayList<>();//单次心率历史数据
    private List<Map<String, String>> listTemp = new ArrayList<>();
    private List<Map<String, String>> listSo2 = new ArrayList<>();
    private int MeasureTimes = 90;
    int dataCount = 0;
    int mood=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        syncData();
        InitUI();
        setOnClickListener();

    }
    private void InitUI() {
        binding.tvDeviceName.setText(Utilities.DeviceName);
        binding.ivConnect.setImageResource(R.drawable.connected);
        String lastReading = Utilities.getValue(this, Utilities.LastReadingTime, "-");
        if (lastReading.equals("-")) {
            Utilities.setValue(this, Utilities.LastReadingTime, Utilities.getCurrentTime());
        }
        binding.tvLastReadingTime.setText(lastReading);
        binding.tvLastUploadTime.setText(Utilities.getValue(this, Utilities.LastUploadTime, "-"));
        subscription = RxBus.getInstance().toObservable(BleData.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<BleData>() {
            @Override
            public void accept(BleData bleData) throws Exception {
                String action = bleData.getAction();
                if (action.equals(BleService.ACTION_GATT_onDescriptorWrite)) {
                    binding.ivConnect.setImageResource(R.drawable.connected);
                    Utilities.dissMissDialog();
                    syncData();
                } else if (action.equals(BleService.ACTION_GATT_DISCONNECTED)) {
                    binding.ivConnect.setImageResource(R.drawable.reconnect);
                    Utilities.dissMissDialog();
                }
            }
        });
    }

    private void syncData() {
        clearList();
        setTime();
        showProgressDialog("Please wait....");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (CheckSelfPermission.isNetworkConnected(MainActivity.this)) {
                    Utilities.setValue(MainActivity.this, Utilities.LastReadingTime, Utilities.getCurrentTime());
                    new LongOperation().execute();
                }else {
                    showToast("No Internet Connection");
                    disMissProgressDialog();
                }
            }
        }, 3000);
    }

    private void setOnClickListener() {
        binding.ivConnect.setOnClickListener(v -> {
            if (BleManager.getInstance().isConnected()) {
                BleManager.getInstance().disconnectDevice();
                binding.ivConnect.setImageResource(R.drawable.reconnect);
            } else {
                if (CheckSelfPermission.isBluetooth12Permission(MainActivity.this)) {
                    if (CheckSelfPermission.isBluetoothOn(this)) {
                        connectDevice();
                        binding.tvLastReadingTime.setText(Utilities.getValue(MainActivity.this, Utilities.LastReadingTime, "-"));
                    }
                }
            }
        });
        binding.ivUpload.setOnClickListener(v -> {
            if (CheckSelfPermission.isNetworkConnected(MainActivity.this)) {
                if (CheckSelfPermission.isBluetoothOn(this)) {
                    if (BleManager.getInstance().isConnected()) {
                        UploadData();
                    } else {
                        showToast(getString(R.string.device_disconnected));
                    }
                }
            } else {
                showToast("No Internet Connection");
            }
        });
        binding.btnEcgMeasure.setOnClickListener(v -> {
            if (CheckSelfPermission.isBluetoothOn(this)) {
                if (CheckSelfPermission.checkStoragePermission(this)) {
                    if (CheckSelfPermission.checkStoragePermissionRetional(this)) {
                        if (BleManager.getInstance().isConnected()) {
                            AppMethods.showProgressDialog(this,getString(R.string.please_wait));
                            getEcgData();
                        }
                    }
                }
            }
        });
        binding.ivSync.setOnClickListener(v -> {
            if (CheckSelfPermission.isBluetooth12Permission(MainActivity.this)) {
                if (CheckSelfPermission.isBluetoothOn(this)) {
                if (BleManager.getInstance().isConnected()) {
                    syncData();
                } else {
                    showToast(getString(R.string.device_disconnected));
                }
            } }
        });

    }

    private void connectDevice() {
        try {
            BleManager.getInstance().connectDevice(Utilities.MacAddress);
            Utilities.showConnectDialog(this);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!BleManager.getInstance().isConnected()) {
                        Utilities.dissMissDialog();
                        Toast.makeText(MainActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 15000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (BleManager.getInstance().isConnected()) BleManager.getInstance().disconnectDevice();
        super.onDestroy();
        unsubscribe();

    }

    private void unsubscribe() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            //   Log.i(TAG, "unSubscribe: ");
        }
    }

    @Override
    public void dataCallback(Map<String, Object> map) {
        super.dataCallback(map);
        String dataType = getDataType(map);
       Log.e("dataCallback :-","data-- "+map.toString());
        switch (dataType) {
            case BleConst.SetDeviceTime:
                sendValue(BleSDK.GetDeviceTime());
                break;
            case BleConst.GetDeviceTime:
                binding.tvCurrentTime.setText(Utilities.getTime(getData(map).get(DeviceKey.DeviceTime)).toUpperCase());
                sendValue(BleSDK.GetDeviceBatteryLevel());
                break;
            case BleConst.GetDeviceBatteryLevel:
                Map<String, String> data = getData(map);
                String battery = data.get(DeviceKey.BatteryLevel);
                try {
                    int batteryLevel = Integer.parseInt(battery);
                    if (batteryLevel >= 0 && batteryLevel <= 10) {
                        binding.ivBattery.setImageResource(R.drawable.b_10);
                    } else if (batteryLevel >= 11 && batteryLevel <= 20) {
                        binding.ivBattery.setImageResource(R.drawable.b_20);
                    } else if (batteryLevel >= 21 && batteryLevel <= 30) {
                        binding.ivBattery.setImageResource(R.drawable.b_30);
                    } else if (batteryLevel >= 31 && batteryLevel <= 40) {
                        binding.ivBattery.setImageResource(R.drawable.b_40);
                    } else if (batteryLevel >= 41 && batteryLevel <= 50) {
                        binding.ivBattery.setImageResource(R.drawable.b_50);
                    } else if (batteryLevel >= 51 && batteryLevel <= 60) {
                        binding.ivBattery.setImageResource(R.drawable.b_60);
                    } else if (batteryLevel >= 61 && batteryLevel <= 70) {
                        binding.ivBattery.setImageResource(R.drawable.b_70);
                    } else if (batteryLevel >= 71 && batteryLevel <= 80) {
                        binding.ivBattery.setImageResource(R.drawable.b_80);
                    } else if (batteryLevel >= 81 && batteryLevel <= 90) {
                        binding.ivBattery.setImageResource(R.drawable.b_90);
                    } else if (batteryLevel >= 91 && batteryLevel <= 100) {
                        binding.ivBattery.setImageResource(R.drawable.b_100);
                    } else {
                        binding.ivBattery.setImageResource(R.drawable.b_100);
                    }
                    setAutoTimeDevice();
                } catch (Exception e) {
                    e.printStackTrace();
                    // Toast.makeText(MainActivity.this,"Real Time Crash" +e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                break;
            case BleConst.GetStaticHR:
                try {
                    boolean end = getEnd(map);
                    dataCount++;
                    listHeart.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                    if (end) {
                        dataCount = 0;
                        getHrvData(ModeStart);
                        saveHeartHistoryData();
                    }
                    if (dataCount == 50) {
                        dataCount = 0;
                        if (end) {
                            getHrvData(ModeStart);
                            saveHeartHistoryData();
                            Log.e("saveHeartHistoryData","vvvvvv111");
                        } else {
                             getStaticHeartHistoryData(ModeContinue);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Toast.makeText(MainActivity.this,"Heat Start Crash" +e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                break;
            case BleConst.GetHRVData:
                try {
                    dataCount++;
                    boolean getend = getEnd(map);
                    listHRV.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                    if (getend) {
                        dataCount = 0;
                        getTempData(ModeStart);
                        saveHrvData();
                    }
                    if (dataCount == 50) {
                        dataCount = 0;
                        if (getend) {
                            getTempData(ModeStart);
                            saveHrvData();
                        } else {
                              getHrvData(ModeContinue);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //  Toast.makeText(MainActivity.this,"HRV Start Crash" +e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                break;
            case BleConst.GetAxillaryTemperatureDataWithMode:
           // case BleConst.Temperature_history:
                try {
                    //showToast(map.toString()+" ---");
                    listTemp.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                    dataCount++;
                    boolean getends = getEnd(map);
                    if (getends) {
                        dataCount = 0;
                        // Toast.makeText(MainActivity.this,"Temp Start " +map.toString(),Toast.LENGTH_SHORT).show();
                        getSO2Data(ModeStart);
                        saveTemp();
                    }
                    if (dataCount == 50) {
                        dataCount = 0;
                        if (getends) {
                            getSO2Data(ModeStart);
                            saveTemp();
                        } else {
                              getTempData(ModeContinue);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
          case BleConst.GetAutomaticSpo2Monitoring:
          //  case BleConst.Blood_oxygen:
                try {
                    listSo2.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                    dataCount++;
                    boolean getSo2ends = getEnd(map);
                    if (getSo2ends) {
                        dataCount = 0;
                        saveS02();
                        getDetailData(ModeStart);
                    }
                    if (dataCount == 50) {
                        dataCount = 0;
                        if (getSo2ends) {
                            saveS02();
                            getDetailData(ModeStart);
                        } else {
                            //getSO2Data(ModeContinue);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case BleConst.GetDetailActivityData:
                listDetail.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                dataCount++;
                boolean getED = getEnd(map);
                if (getED) {
                    dataCount = 0;
                    saveStepDetailData();
                    Utilities.setValue(MainActivity.this, Utilities.LastUploadTime, Utilities.getCurrentTime());
                    binding.tvLastUploadTime.setText(Utilities.getValue(MainActivity.this, Utilities.LastUploadTime, "-"));
                    sendValue(BleSDK.RealTimeStep(true, true));
                    getSleepData(ModeStart);
                    //disMissProgressDialog();
                    //detailDataAdapter.setData(list,DetailDataAdapter.GET_STEP_DETAIL);
                }
                if (dataCount == 50) {
                    dataCount = 0;
                    if (getED) {
                        saveStepDetailData();
                        Utilities.setValue(MainActivity.this, Utilities.LastUploadTime, Utilities.getCurrentTime());
                        binding.tvLastUploadTime.setText(Utilities.getValue(MainActivity.this, Utilities.LastUploadTime, "-"));
                        sendValue(BleSDK.RealTimeStep(true, true));
                        getSleepData(ModeStart);
                        //  disMissProgressDialog();
                        // detailDataAdapter.setData(list,DetailDataAdapter.GET_STEP_DETAIL);
                    } else {
                          getDetailData(ModeContinue);
                    }
                }
                break;

            case BleConst.RealTimeStep:
                try {
                    Map<String, String> maps = getData(map);
                    String step = maps.get(DeviceKey.Step);
                    String cal = maps.get(DeviceKey.Calories);
                    String distance = maps.get(DeviceKey.Distance);
                    String heart = maps.get(DeviceKey.HeartRate);
                    String TEMP = maps.get(DeviceKey.TempData);
                    binding.tvKCal.setText(cal);
                    binding.tvStep.setText(step);
                    binding.tvDistanceKm.setText(distance);
                    binding.tvHeartValue.setText(heart);
                } catch (Exception e) {
                    e.printStackTrace();
                    //   Toast.makeText(MainActivity.this,"Real Time Crash" +e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                break;
                 case BleConst.GetDetailSleepData:
                try {
                    boolean finish = getEnd(map);
                    dataCount++;
                    listSleep.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                    if (finish) {
                        dataCount = 0;
                        saveSleepData();
                    }
                    if (dataCount == 50) {
                        dataCount = 0;
                        if (finish) {
                            saveSleepData();
                        } else {
                            getSleepData(ModeContinue);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                   // Toast.makeText(MainActivity.this,"Sleep Start Crash" +e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                break;
                case BleConst.ECGdata:
                boolean finish=getEnd(map);
                Map<String,String>ecgmaps= getData(map);
                if (null!=ecgmaps.get(DeviceKey.Date)) {
                    String KDate = ecgmaps.get(DeviceKey.Date);
                    ecgDate = KDate;
                    stringBuffer = new StringBuffer();
                    healthEcgData = new EcgHistoryData();
                    int hrv = Integer.parseInt(ecgmaps.get(DeviceKey.HRV));
                    int hr = Integer.parseInt(ecgmaps.get(DeviceKey.HeartRate));
                    int moodValue = Integer.parseInt(ecgmaps.get(DeviceKey.ECGMoodValue));
                    mood=moodValue;
                    healthEcgData.setTime(ecgDate);
                    healthEcgData.setHrv(hrv);
                    healthEcgData.setHeartRate(hr);
                    healthEcgData.setBreathValue(moodValue);
                    healthEcgData.setAddress(address);
                }
                if (null!=ecgmaps.get(DeviceKey.ECGValue)) {
                    String ecgData = ecgmaps.get(DeviceKey.ECGValue);
                    stringBuffer.append(ecgData);
                }if (finish) {
                    if (!TextUtils.isEmpty(ecgDate)) {
                        healthEcgData.setArrayECGData(stringBuffer.toString());
                        ecgDataList.add(healthEcgData);
                        AppMethods.hideProgressDialog(this);
                        if(ecgDataList.size()!=0){
                            Intent intent=    new Intent(this,EcgReportActivity.class);
                            intent.putExtra("ecgData", (Serializable) ecgDataList)  ;
                            startActivity(intent);
                        }
                        Log.e("ecgData","isEmpty(ecgDate)"+ecgDate+"***"+ecgDataList.size());
                        if ( !ecgDate.equals(lastEcgDate) &&index < 9) {
                            index++;
                            ecgDate = "";
                            //sendValue(BleSDK.GetECGwaveform(true,index,lastEcgDate));
                        } else {
                            ecgDate = "";
                            index = 0;
                      // last
                        }
                    } else {
                        ecgDate = "";
                        index = 0;
                        AppMethods.hideProgressDialog(this);
                        Intent intent=    new Intent(this,EcgReportActivity.class);
                        intent.putExtra("ecgData", (Serializable) ecgDataList)  ;
                        startActivity(intent);
                    // lastEcgDate
                    }
                }
            case BleConst.SetPersonalInfo:
                break;
            case BleConst.GetPersonalInfo:
               /* String age=data.get(DeviceKey.Age);
                String height=data.get(DeviceKey.Height);
                String weight=data.get(DeviceKey.Weight);
                String stepLength=data.get(DeviceKey.Stride);
                int gender= Integer.parseInt(data.get(DeviceKey.Gender));*/
                break;
        }
    }

    int index=0;
    private  String ecgDate;
    private String lastEcgDate;
    StringBuffer   stringBuffer;
    EcgHistoryData healthEcgData;
    String address="";
    ArrayList<EcgHistoryData> ecgDataList = new ArrayList<>();

    final List<StepDetailData> stepDataList = new ArrayList<>();

    private void saveStepDetailData() {
        try {
            for (Map<String, String> map : listDetail) {
                StepDetailData stepData = new StepDetailData();
                String date = map.get(DeviceKey.Date);
                if(Utilities.isDeviceYear(date)) {
                    String totalStep = map.get(DeviceKey.KDetailMinterStep);
                    String distance = map.get(DeviceKey.Distance);
                    String cal = map.get(DeviceKey.Calories);
                    String detailStep = map.get(DeviceKey.ArraySteps);
                    String dateMatch = Utilities.getDeciveDate(date);
                    String deviceTime = Utilities.getDeciveTime(date);
                    if (dateMatch != null && deviceTime != null) {
                        stepData.setDate(date);
                        stepData.setStep(totalStep);
                        stepData.setDistance(distance);
                        stepData.setCal(cal);
                        stepData.setMinterStep(detailStep);
                        stepDataList.add(stepData);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<ValuesModel> so2List = new ArrayList<>();
    private void saveS02() {
        try {
            ArrayList<Integer> so2IntList = new ArrayList<>();
            int avg = 0, count = 0;
            for (Map<String, String> map : listSo2) {
                String time = map.get(DeviceKey.Date);
                if(Utilities.isDeviceYear(time)){
                    int s02 = Utilities.getValueInt(map.get(DeviceKey.Blood_oxygen));
                    String dateMatch = Utilities.getDeciveDate(time);
                    String deviceTime= Utilities.getDeciveTime(time);
                    if(dateMatch!=null &&deviceTime!=null) {
                        binding.tvBloodOxy.setText(listSo2.get(0).get(DeviceKey.Blood_oxygen) + "%");
                        so2List.add(new ValuesModel(s02, time));
                        so2IntList.add(s02);
                        avg += s02;
                        count += 1;
                    }
                }
            }
            binding.tvMaxOxygen.setText(Collections.max(so2IntList) + "%");
            binding.tvMinOxygen.setText(Collections.min(so2IntList) + "%");
            binding.tvAvgOxygen.setText((avg / count) + "%");
        } catch (Exception e) {
            e.printStackTrace();
            //    Toast.makeText(MainActivity.this,"SO2 Values Crash "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    ArrayList<HrvModel> hrvList = new ArrayList<>();

    private void saveHrvData() {
        try {
            int avg = 0, avgStress = 0;
            int count = 0;
            ArrayList<Integer> hrvIntList = new ArrayList<>();
            ArrayList<Integer> stressList = new ArrayList<>();
            for (Map<String, String> map : listHRV) {
                String time = map.get(DeviceKey.Date);
                if(Utilities.isDeviceYear(time)){
                int lowBp = Utilities.getValueInt(map.get(DeviceKey.lowBP));
                int HighBp = Utilities.getValueInt(map.get(DeviceKey.highBP));
                int Stress = Utilities.getValueInt(map.get(DeviceKey.Stress));
                int hrv = Utilities.getValueInt(map.get(DeviceKey.HRV));
                String dateMatch = Utilities.getDeciveDate(time);
                String deviceTime= Utilities.getDeciveTime(time);
                if(dateMatch!=null &&deviceTime!=null) {
                    hrvIntList.add(hrv);
                    stressList.add(Stress);
                    hrvList.add(new HrvModel(hrv, Stress, lowBp, HighBp, time));
                    binding.tvBloodPressure.setText(HighBp + "/" + lowBp + " mmHg");
                    avgStress += Stress;
                    avg += hrv;
                    count += 1;
                }
                }
            }
            binding.tvAvgStress.setText((avgStress / count) + "");
            binding.tvMaxStress.setText(Collections.max(stressList) + "");
            binding.tvMinStress.setText(Collections.min(stressList) + "");
            binding.tvMaxHrv.setText(Collections.max(hrvIntList) + "");
            binding.tvMinHrv.setText(Collections.min(hrvIntList) + "");
            binding.tvAvgHrv.setText((avg / count) + "");
            binding.tvHrvEcg.setText(""+(avg / count));

        } catch (Exception e) {
            e.printStackTrace();
            //    Toast.makeText(MainActivity.this,"Hrv Values Crash "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }

    }

    ArrayList<ValuesModel> sleepList=new ArrayList<>();
    private void saveSleepData() {
        try {
            int Sleep = 0;
            for (Map<String, String> map : listSleep) {
                String time = map.get(DeviceKey.Date);
                if(Utilities.isDeviceSleepYear(time)) {
                    String[] sleepQuantity = map.get(DeviceKey.ArraySleep).split(" ");
                    String dateMatch = Utilities.getSleepDate(time);
                    String deviceTime = Utilities.getSleepTime(time);
                    if (dateMatch != null && deviceTime != null) {
                        int SleepQuantity = 0;
                        for (int i = 0; i < sleepQuantity.length; i++) {
                            SleepQuantity += Utilities.getValueInt(sleepQuantity[i]);
                            Sleep += SleepQuantity;
                        }
                        sleepList.add(new ValuesModel(SleepQuantity, time));
                    }
                }
            }
            binding.tvSleepQuality.setText(String.valueOf(Sleep));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ArrayList<HeartRateModel> heartList = new ArrayList<>();

    private void saveHeartHistoryData() {
        try {
            ArrayList<Integer> heartIntList = new ArrayList<>();
            int avg = 0;
            int count = 0;
            for (Map<String, String> map : listHeart) {
                String time = map.get(DeviceKey.Date);
                String hrString = map.get(DeviceKey.StaticHR);
                if(Utilities.isDeviceYear(time)) {
                    int hr = Utilities.getValueInt(hrString);
                    if (hr != 0) {
                        String dateMatch = Utilities.getDeciveDate(time);
                        String deviceTime = Utilities.getDeciveTime(time);
                        if (dateMatch != null && deviceTime != null) {
                            heartList.add(new HeartRateModel(hr, time));
                            heartIntList.add(hr);
                            avg += hr;
                            count += 1;
                        }
                    }
                }
            }
            Log.e("heartSize",heartIntList.size()+" "+heartList.size());
            binding.tvHighestValue.setText(Collections.max(heartIntList) + "");
            binding.tvLowestValue.setText(Collections.min(heartIntList) + "");
            binding.tvAvgValue.setText((avg / count) + "");
            binding.tvAvgHrvEcg.setText(""+(avg / count));
        } catch (Exception e) {
            e.printStackTrace();
            // Toast.makeText(MainActivity.this,"HeartValues Crash "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void clearList() {
        hrvList.clear();
        heartList.clear();
        so2List.clear();
        sleepList.clear();
        stepDataList.clear();
        Templist.clear();
        list.clear();
        listSo2.clear();
        listHeart.clear();
        listHRV.clear();
        listSleep.clear();
        listTemp.clear();
        listDetail.clear();
    }

    private void UploadData() {
            try {
                showProgressDialog(getString(R.string.please_wait));
                new LongOperation().execute();
            } catch (Exception e) {
            e.printStackTrace();
                  disMissProgressDialog();
            }
    }

    private void setTime() {
        java.util.Calendar calendar= java.util.Calendar.getInstance();
        int year=calendar.get(Calendar.YEAR);
        int month=calendar.get(Calendar.MONTH)+1;
        int day=calendar.get(Calendar.DAY_OF_MONTH);
        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int min=calendar.get(Calendar.MINUTE);
        int second=calendar.get(Calendar.SECOND);
        MyDeviceTime setTime=new MyDeviceTime();
        setTime.setYear(year);
        setTime.setMonth(month);
        setTime.setDay(day);
        setTime.setHour(hour);
        setTime.setMinute(min);
        setTime.setSecond(second);
        sendValue(BleSDK.SetDeviceTime(setTime));
    }
    private class LongOperation extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (CheckSelfPermission.isNetworkConnected(MainActivity.this)) {
                    dataUpload();
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e("dataUpload","data Parsing error"+e.getMessage());
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    disMissProgressDialog();
                    showToast("Data is Upload");
                }
            }, 8000);
        }
    }

    private void dataUpload() {
        FirebaseFirestore  db = FirebaseFirestore.getInstance();
        if(heartList.size()!=0) {
            Map<String, Object> hh = new HashMap<>();
            String dateMatch = Utilities.getDeciveDate(heartList.get(0).getDate());
            hh.put(Utilities.getDeciveTime(heartList.get(0).getDate()),getHashMap(heartList.get(0).getValue()));
            for (int i = 0; i < heartList.size(); i++) {
                String time = Utilities.getDeciveTime(heartList.get(i).getDate());
                String date= Utilities.getDeciveDate(heartList.get(i).getDate());
                if (!dateMatch.equals(date)) {
                    dateMatch = date;
                    addData(db,FireBaseKey.FIREBASE_HeartRate,Utilities.getDeciveDate(heartList.get(i-1).getDate()),hh);
                    hh.clear();
                    hh.put(time,getHashMap(heartList.get(i).getValue()));
                }else {
                    hh.put(time,getHashMap(heartList.get(i).getValue()));
                    if(heartList.size()==i+1){
                        addData(db,FireBaseKey.FIREBASE_HeartRate,date,hh);
                        hh.clear();
                    }
                }
            }
        }
        if(so2List.size()!=0){
            Map<String, Object> hashMap = new HashMap<>();
            String dateMatch = Utilities.getDeciveDate(so2List.get(0).getDate());
            hashMap.put(Utilities.getDeciveTime(so2List.get(0).getDate()),getHashMap(so2List.get(0).getValue()));
            for (int i = 0; i < so2List.size(); i++) {
                String time= Utilities.getDeciveTime(so2List.get(i).getDate());
                String date=Utilities.getDeciveDate(so2List.get(i).getDate());
                if (!dateMatch.equals(date)) {
                    dateMatch = date;
                    addData(db,FireBaseKey.FIREBASE_SPO2,Utilities.getDeciveDate(so2List.get(i - 1).getDate()),hashMap);
                    hashMap.clear();
                    hashMap.put(time,getHashMap(so2List.get(i).getValue()));
                }else {
                    hashMap.put(time,getHashMap(so2List.get(i).getValue()));
                    if(so2List.size()==i+1){
                        addData(db,FireBaseKey.FIREBASE_SPO2,date,hashMap);
                        hashMap.clear();
                    }
                }
            }
        }
        if(stepDataList.size()!=0){
            Map<String, Object> hashMap = new HashMap<>();
            String dateMatch = Utilities.getDeciveDate(stepDataList.get(0).getDate());
            hashMap.put(Utilities.getDeciveTime(stepDataList.get(0).getDate()),getStepMap(stepDataList.get(0)));
            for (int i = 0; i < stepDataList.size(); i++) {
                String time= Utilities.getDeciveTime(stepDataList.get(i).getDate());
                String date=Utilities.getDeciveDate(stepDataList.get(i).getDate());
                if (!dateMatch.equals(date)) {
                    dateMatch=date;
                    addData(db,FireBaseKey.FIREBASE_ActivityDetail,Utilities.getDeciveDate(stepDataList.get(i-1).getDate()),hashMap);
                    hashMap.clear();
                    hashMap.put(time,getStepMap(stepDataList.get(i)));
                }else {
                    hashMap.put(time,getStepMap(stepDataList.get(i)));
                    if(stepDataList.size()==i+1){
                        addData(db,FireBaseKey.FIREBASE_ActivityDetail,date,hashMap);
                        hashMap.clear();
                    }
                }
            }
        }
        if(Templist.size()!=0){
            Map<String, Object> hashMap = new HashMap<>();
            String dateMatch = Utilities.getDeciveDate(Templist.get(0).getDate());
            hashMap.put(Utilities.getDeciveTime(Templist.get(0).getDate()),getTempMap(Templist.get(0).getValue()));
            for (int i = 0; i < Templist.size(); i++) {
              String time= Utilities.getDeciveTime(Templist.get(i).getDate());
              String date= Utilities.getDeciveDate(Templist.get(i).getDate());
                if (!dateMatch.equals(date)) {
                    dateMatch=date;
                    addData(db,FireBaseKey.FIREBASE_Temperature,Utilities.getDeciveDate(Templist.get(i-1).getDate()),hashMap);
                    hashMap.clear();
                    hashMap.put(time,getTempMap(Templist.get(i).getValue()));
                }else {
                    hashMap.put(time,getTempMap(Templist.get(i).getValue()));
                    if(Templist.size()==i+1){
                        addData(db,FireBaseKey.FIREBASE_Temperature,date,hashMap);
                        hashMap.clear();
                    }
                }
            }
        }
        if(hrvList.size()!=0){
            Map<String, Object> hashMap = new HashMap<>();
            Map<String, Object> stressMap = new HashMap<>();
            Map<String, Object> bpMap = new HashMap<>();
            String dateMatch = Utilities.getDeciveDate(hrvList.get(0).getDate());
            String dateStress = Utilities.getDeciveDate(hrvList.get(0).getDate());
            String dateBp = Utilities.getDeciveDate(hrvList.get(0).getDate());
            bpMap.put(Utilities.getDeciveTime(hrvList.get(0).getDate()),getBloodMap(hrvList.get(0)));
            stressMap.put(Utilities.getDeciveTime(hrvList.get(0).getDate()),getHashMap(hrvList.get(0).getStress()));
            hashMap.put(Utilities.getDeciveTime(hrvList.get(0).getDate()),getHashMap(hrvList.get(0).getHrv()));
            for (int i = 0; i < hrvList.size(); i++) {
                String date= Utilities.getDeciveDate(hrvList.get(i).getDate());
                String time= Utilities.getDeciveTime(hrvList.get(i).getDate());
                if (hrvList.get(i).getHrv() != 0) {
                    if (!dateMatch.equals(date)) {
                        dateMatch = date;
                        addData(db,FireBaseKey.FIREBASE_HRV,Utilities.getDeciveDate(hrvList.get(i-1).getDate()),hashMap);
                        hashMap.clear();
                        hashMap.put(time,getHashMap(hrvList.get(i).getHrv()));
                    }else {
                        hashMap.put(time,getHashMap(hrvList.get(i).getHrv()));
                        if(hrvList.size()==i+1){
                            addData(db,FireBaseKey.FIREBASE_HRV,date,hashMap);
                            hashMap.clear();
                        }
                    }
                }
                if (hrvList.get(i).getStress() != 0) {
                    if (!dateStress.equals(date)) {
                        dateStress=date;
                        addData(db,FireBaseKey.FIREBASE_Stress,Utilities.getDeciveDate(hrvList.get(i-1).getDate()),stressMap);
                        stressMap.clear();
                        stressMap.put(time,getHashMap(hrvList.get(i).getStress()));
                    }else {
                        stressMap.put(time,getHashMap(hrvList.get(i).getStress()));
                        if(hrvList.size()==i+1){
                            addData(db,FireBaseKey.FIREBASE_Stress,date,stressMap);
                            stressMap.clear();
                        }
                    }
                }
                if (hrvList.get(i).getHighBp() != 0) {
                    if(!dateBp.equals(date)){
                        dateBp=date;
                        addData(db,FireBaseKey.FIREBASE_Blood_pressure,Utilities.getDeciveDate(hrvList.get(i-1).getDate()),bpMap);
                        bpMap.clear();
                        bpMap.put(time,getBloodMap(hrvList.get(i)));
                    }else {
                        bpMap.put(time,getBloodMap(hrvList.get(i)));
                        if(hrvList.size()==i+1){
                            addData(db,FireBaseKey.FIREBASE_Blood_pressure,date,bpMap);
                            bpMap.clear();
                        }
                    }
                }
            }
        }
        if(sleepList.size()!=0){
            try {
                Map<String, Object> hashMap = new HashMap<>();
                String dateMatch = Utilities.getSleepDate(sleepList.get(0).getDate());
                hashMap.put(Utilities.getSleepTime(sleepList.get(0).getDate()),getHashMap(sleepList.get(0).getValue()));
                for (int i=0;i<sleepList.size();i++) {
                   String  date= Utilities.getSleepDate(sleepList.get(i).getDate());
                   String  time= Utilities.getSleepTime(sleepList.get(i).getDate());
                    if (!dateMatch.equals(date)){
                        dateMatch=date;
                        addData(db,FireBaseKey.FIREBASE_SleepQuality,Utilities.getSleepDate(sleepList.get(i-1).getDate()),hashMap);
                        hashMap.clear();
                        hashMap.put(time,getHashMap(sleepList.get(i).getValue()));
                    }else {
                        hashMap.put(time,getHashMap(sleepList.get(i).getValue()));
                        if(sleepList.size()==i+1){
                            addData(db,FireBaseKey.FIREBASE_SleepQuality,date,hashMap);
                            hashMap.clear();
                        }
                    }
             }
            }catch (Exception e){
                e.printStackTrace();
                disMissProgressDialog();
            }
        }

        /*if (CheckSelfPermission.isNetworkConnected(this)) {
            Map<String, Object> EcgMap = new HashMap<>();
            EcgMap.put(FireBaseKey.HrvEcg, Utilities.getValueInt(binding.tvHrvEcg.getText().toString()));
            EcgMap.put(FireBaseKey.HrEcg, Utilities.getValueInt(binding.tvAvgHrvEcg.getText().toString()));
            EcgMap.put(FireBaseKey.Mood,mood);
            db.collection(FireBaseKey.FIREBASE_COLLECTION_NAME).
                    document(Utilities.MacAddress)
                    .collection(FireBaseKey.FIREBASE_ECG_PPG)
                    .document(Utilities.getCurrentDate())
                    .set(Utilities.getTimeHashmap(EcgMap), SetOptions.merge());
        }*/
    }

    private void getStaticHeartHistoryData(byte mode) {
        sendValue(BleSDK.GetStaticHRWithMode(mode,""));
    }

    private void getDetailData(byte mode) {
        sendValue(BleSDK.GetDetailActivityDataWithMode(mode,""));
    }

    private void getDynamicHeartHistoryData(byte mode) {
        sendValue(BleSDK.GetDynamicHRWithMode(mode,""));
    }

    private void getEcgData(){
        sendValue(BleSDK.getEcgHistoryData(0,""));
        //  sendValue(BleSDK.GetECGwaveform(0x00));
       // sendValue(BleSDK.GetECGwaveform(true,1));
      //  sendValue(BleSDK.GetECGwaveform(true,0));
    }
    /*public static byte[] GetECGwaveform(boolean readdata, int num) {
        read = readdata;
        byte[] value = new byte[16];
        value[0] = 113;
        value[1] = (byte)(readdata ? 0 : -103);
        value[2] = (byte)num;
        BleSDK.crcValue(value);
        return value;
    }*/
    private void getSleepData(byte mode) {
        sendValue(BleSDK.GetDetailSleepDataWithMode(mode,""));
    }

    private void getHrvData(byte mode) {
        sendValue(BleSDK.GetHRVDataWithMode(mode,""));
    }

    private void getTempData(byte mode) {
     //   sendValue(BleSDK.GetTemperature_historyDataWithMode(mode,""));
        sendValue(BleSDK.GetAxillaryTemperatureDataWithMode(mode,""));
    }
    private void getSO2Data(byte mode) {

        sendValue(BleSDK.Obtain_The_data_of_manual_blood_oxygen_test(mode));
     //   sendValue(BleSDK.GetBloodOxygen(mode,""));
    }
   private  Map<String, Object> getHashMap(int value){
       Map<String, Object> HeartMap = new HashMap<>();
       HeartMap.put(FireBaseKey.Values, value);
       HeartMap.put(FireBaseKey.FIREBASE_OS, FireBaseKey.ANDROID);
       return HeartMap;
    }
    private  Map<String, Object> getStepMap(StepDetailData stepDetailData){
        Map<String, Object> activityDetail = new HashMap<>();
        activityDetail.put(FireBaseKey.Step, Utilities.getValueInt(stepDetailData.getStep()));
        activityDetail.put(FireBaseKey.Kcal, Utilities.getValueFloat(stepDetailData.getCal()));
        activityDetail.put(FireBaseKey.Distance, Utilities.getValueFloat(stepDetailData.getDistance()));
        activityDetail.put(FireBaseKey.FIREBASE_OS,FireBaseKey.ANDROID);
        return activityDetail;
    }
    private Map<String, Object> getBloodMap(HrvModel hrvModel){
        Map<String, Object> BloodPress = new HashMap<>();
        BloodPress.put(FireBaseKey.Values, hrvModel.getHighBp() + "/" + hrvModel.getLowBp());
        BloodPress.put(FireBaseKey.FIREBASE_OS,FireBaseKey.ANDROID);
        return BloodPress;
    }
    private Map<String, Object> getTempMap(Float value){
        Map<String, Object> TempMap = new HashMap<>();
        TempMap.put(FireBaseKey.Values,value);
        TempMap.put(FireBaseKey.FIREBASE_OS,FireBaseKey.ANDROID);
        return TempMap;
    }
   public void addData(FirebaseFirestore db,String key,String date,Map<String, Object> hashMapTime){
       db.collection(FireBaseKey.FIREBASE_COLLECTION_NAME)
               .document(Utilities.MacAddress)
               .collection(key)
               .document(date)
               .set(hashMapTime,SetOptions.merge());
   }
    private void setAutoTimeDevice(){
        try {
            sendValue(BleSDK.SetAutomatic(true,1,AutoMode.AutoHrv));
            Thread.sleep(100);
            sendValue(BleSDK.SetAutomatic(true,1,AutoMode.AutoSpo2));
            Thread.sleep(100);
            sendValue(BleSDK.SetAutomatic(true,1,AutoMode.AutoHeartRate));
            Thread.sleep(100);
            sendValue(BleSDK.SetAutomatic(true,1,AutoMode.AutoTemp));
            Thread.sleep(100);
            getStaticHeartHistoryData(ModeStart);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    ArrayList<TempModel> Templist = new ArrayList<>();

    private void saveTemp() {
        try {
            double avg = 0;
            int count = 0;
            ArrayList<Float> tempList = new ArrayList<>();
            for (Map<String, String> map : listTemp) {
              //  float temp = Utilities.getValueFloat(map.get(DeviceKey.temperature));
                float temp=Utilities.getValueFloat(map.get(DeviceKey.axillaryTemperature));
                String time = map.get(DeviceKey.Date);
                if(temp>0){
                    Templist.add(new TempModel(temp,time));
                    tempList.add(temp);
                    avg += temp;
                    count += 1;
                }
            }
            binding.tvAvgTemp.setText(Math.round(avg / count)  + "");
            binding.tvMaxTemp.setText(Collections.max(tempList) + "");
            binding.tvMinTemp.setText(Collections.min(tempList) + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}