package com.ble.healthmonitoringapp.activity;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.ble.BleManager;
import com.ble.healthmonitoringapp.ble.BleService;
import com.ble.healthmonitoringapp.databinding.ActivityMainBinding;
import com.ble.healthmonitoringapp.utils.BleData;
import com.ble.healthmonitoringapp.utils.RxBus;
import com.ble.healthmonitoringapp.utils.Utilities;
import com.jstyle.blesdk2025.Util.BleSDK;
import com.jstyle.blesdk2025.constant.BleConst;
import com.jstyle.blesdk2025.constant.DeviceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    ActivityMainBinding binding;
    private Disposable subscription;
    int ModeStart = 0;
    int ModeContinue = 2;
    int ModeDelete = 0x99;
    private String date;
    List<Map<String, String>> list = new ArrayList<>();
    private List<Map<String, String>> listHRV = new ArrayList<>();
    private List<Map<String, String>> listSleep = new ArrayList<>();
    private List<Map<String, String>> listHeart = new ArrayList<>();//单次心率历史数据
    private List<Map<String, String>> listTemp = new ArrayList<>();
    int dataCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        sendValue(BleSDK.GetDeviceTime());
        InitUI();
        setOnClickListener();
    }

    private void InitUI(){
        binding.tvDeviceName.setText(Utilities.DeviceName);
        binding.ivConnect.setImageResource(R.drawable.connected);
        subscription = RxBus.getInstance().toObservable(BleData.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<BleData>() {
            @Override
            public void accept(BleData bleData) throws Exception {
                String action = bleData.getAction();
                if (action.equals(BleService.ACTION_GATT_onDescriptorWrite)) {
                    binding.ivConnect.setImageResource(R.drawable.connected);
                    sendValue(BleSDK.GetDeviceTime());
                    Utilities.dissMissDialog();
                } else if (action.equals(BleService.ACTION_GATT_DISCONNECTED)) {
                    binding.ivConnect.setImageResource(R.drawable.reconnect);
                    Utilities.dissMissDialog();
                }
            }
        });

    }
    private void setOnClickListener(){
        binding.ivConnect.setOnClickListener(v->{
            if (BleManager.getInstance().isConnected()){
                BleManager.getInstance().disconnectDevice();
                binding.ivConnect.setImageResource(R.drawable.reconnect);
            }else {
                connectDevice();
            }
        });
    }
    private void connectDevice(){
        BleManager.getInstance().connectDevice(Utilities.MacAddress);
        Utilities.showConnectDialog(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!BleManager.getInstance().isConnected()){
                    Utilities.dissMissDialog();
                    Toast.makeText(MainActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        },20000);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
        if (BleManager.getInstance().isConnected()) BleManager.getInstance().disconnectDevice();
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
        String dataType= getDataType(map);
        Map<String,String>data= getData(map);
        switch (dataType){
            case BleConst.GetDeviceTime:
                binding.tvCurrentTime.setText(data.get(DeviceKey.DeviceTime));
                sendValue(BleSDK.GetDeviceBatteryLevel());
                break;
                case BleConst.SetPersonalInfo:
               break;
            case BleConst.GetPersonalInfo:
                String age=data.get(DeviceKey.Age);
                String height=data.get(DeviceKey.Height);
                String weight=data.get(DeviceKey.Weight);
                String stepLength=data.get(DeviceKey.Stride);
                int gender= Integer.parseInt(data.get(DeviceKey.Gender));
                break;
            case BleConst.RealTimeStep:
                getSleepData(ModeStart);
                Map<String, String> maps = getData(map);
                String step = maps.get(DeviceKey.Step);
                String cal = maps.get(DeviceKey.Calories);
                String distance = maps.get(DeviceKey.Distance);
                String time = maps.get(DeviceKey.ExerciseMinutes);
                String ActiveTime = maps.get(DeviceKey.ActiveMinutes);
                String heart = maps.get(DeviceKey.HeartRate);
                String TEMP= maps.get(DeviceKey.TempData);
                binding.tvKCal.setText(cal);
                binding.tvStep.setText(step);
                binding.tvDistanceKm.setText(distance);
                binding.tvHeartValue.setText(heart);
                binding.tvAvgTemp.setText(TEMP);
                break;
            case BleConst.GetDeviceBatteryLevel:
                sendValue(BleSDK.RealTimeStep(true,true));
                String battery = data.get(DeviceKey.BatteryLevel);
                try {
                    int batteryLevel= Integer.parseInt(battery);
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
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case BleConst.GetDetailSleepData:
                boolean finish = getEnd(map);
                dataCount++;
                listSleep.addAll((List<Map<String, String>>)map.get(DeviceKey.Data));
                if (finish) {
                    dataCount=0;
                    getStaticHeartHistoryData(ModeStart);
                    saveSleepData();
                }
                if (dataCount == 50) {
                    dataCount = 0;
                    if (finish) {
                        getStaticHeartHistoryData(ModeStart);
                        saveSleepData();
                    } else {
                        getSleepData(ModeContinue);
                    }
                }
                break;
            case BleConst.GetStaticHR:
                boolean end = getEnd(map);
                dataCount++;
                listHeart.addAll((List<Map<String,String>>) map.get(DeviceKey.Data));
                if(end){
                    dataCount=0;
                    getHrvData(ModeStart);
                    saveHeartHistoryData();
                }
                if(dataCount==50){
                    dataCount=0;
                    if(end){
                        getHrvData(ModeStart);
                        saveHeartHistoryData();
                    }else{
                        getStaticHeartHistoryData(ModeContinue);
                    }
                }
                break;
            case BleConst.GetHRVData:
                dataCount++;
                boolean getend = getEnd(map);
                listHRV.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                if (getend) {
                 dataCount=0;
                 getTempData(ModeStart);
                 saveHrvData();
                }
                if (dataCount == 50) {
                   dataCount=0;
                    if (getend) {
                        getTempData(ModeStart);
                        saveHrvData();
                    } else {
                        getHrvData(ModeContinue);
                    }
                }
                break;
            case BleConst.Temperature_history:
                listTemp.addAll((List<Map<String, String>>) map.get(DeviceKey.Data));
                dataCount++;
                boolean getends = getEnd(map);
                if(getends){
                    dataCount=0;
                }
                if(dataCount==50){
                    Log.e("sdadaa","sssssssssssssssssssss");
                    dataCount=0;
                    if(getends){
                    }else{
                        getTempData(ModeContinue);
                    }
                }
                break;
        }
    }
    private void saveHrvData(){
        for (Map<String, String> map : listHRV) {
               map.get(DeviceKey.HighPressure);
               map.get(DeviceKey.LowPressure);
               map.get(DeviceKey.HRV);
        }
        }

    private void saveSleepData() {
        int SleepQuantity=0;
        //final List<SleepData> sleepDataList = new ArrayList<>();
        for (Map<String, String> map : listSleep) {
            List<String> listHealth = new ArrayList<>();
            String time = map.get(DeviceKey.Date);
          //  long startMil = DateUtil.getDefaultLongMi(time);
            String[] sleepQuantity = map.get(DeviceKey.ArraySleep).split(" ");
            for (int i = 0; i < sleepQuantity.length; i++) {
                SleepQuantity+=Utilities.getValueInt(sleepQuantity[i]);
             //   SleepData sleepData = new SleepData();
             //   sleepData.setAddress(deviceAddress);
              //  sleepData.setDateString(sleepQuantity[i]);
              //  long timeMil = startMil + 1 * 60 * 1000l * i;
                //sleepData.setTime(DateUtil.getFormatTimeString(timeMil));
              //  sleepDataList.add(sleepData);
               // listHealth.add(sleepQuantity[i]);
            }
        }
        binding.tvSleepQuality.setText(String.valueOf(SleepQuantity));

    }
    private void saveHeartHistoryData() {
        int lowest =0,highest=0,avg=0;
        int count=0;
        for (Map<String, String> map : listHeart) {
            List<String> listHealth = new ArrayList<>();
            String hrString = map.get(DeviceKey.StaticHR);
                int hr = Utilities.getValueInt(hrString);
                if (hr != 0) {
                    avg+=hr;
                    count+=1;
                    if(hr>highest){
                        highest=hr;
                    }
                    if(hr <lowest){
                        lowest=hr;
                    }
                    listHealth.add(String.valueOf(hr));
                }
            }

        binding.tvHighestValue.setText(highest+"");
        binding.tvLowestValue.setText(lowest+"");
        binding.tvAvgValue.setText((avg/count)+"");
    }






    private void getStaticHeartHistoryData(int mode){
        sendValue(BleSDK.GetStaticHRWithMode(mode));
    }
    private void getDetailData(int mode) {
        sendValue(BleSDK.GetDetailActivityDataWithMode(mode));
    }
    private void getDynamicHeartHistoryData(int mode){
        sendValue(BleSDK.GetDynamicHRWithMode(mode));
    }
    private void getSleepData(int mode) {
        sendValue(BleSDK.GetDetailSleepDataWithMode(mode));
    }
    private void getHrvData(int mode) {
        sendValue(BleSDK.GetHRVDataWithMode(mode));
    }
    private void getTempData(int mode){
        sendValue(BleSDK.GetTemperature_historyDataWithMode(mode));
    }
}