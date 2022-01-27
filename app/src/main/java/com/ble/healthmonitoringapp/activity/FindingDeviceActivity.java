package com.ble.healthmonitoringapp.activity;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.ble.healthmonitoringapp.activity.GlobalApplication.mLocation;
import static com.ble.healthmonitoringapp.ble.BleDeviceActor.reconnectDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.ble.BleCharacteristic;
import com.ble.healthmonitoringapp.ble.BleDeviceActor;
import com.ble.healthmonitoringapp.dialog.devicesDialog;
import com.ble.healthmonitoringapp.model.ConnectDeviceModel;
import com.ble.healthmonitoringapp.utils.AppConstants;
import com.ble.healthmonitoringapp.utils.AppMethods;
import com.ble.healthmonitoringapp.utils.BleCallback;
import com.ble.healthmonitoringapp.utils.CheckSelfPermission;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FindingDeviceActivity extends AppCompatActivity implements View.OnClickListener, BleCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private ImageView iv_src,iv_find_dvs;
    private boolean isFirstLocationPermission = true;
    public static BleDeviceActor bleDeviceActor = null;
    private ArrayList<ConnectDeviceModel> deviceModelArrayList = new ArrayList<>();
   private Timer timer;
    private TimerTask timerTask;
    public static int connectPosition = 0;
    public static int currentDevice;
    public static String isConnectedDevice = "false";
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Date today;
    String dateToStr;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finding_device);
        bleDeviceActor = new BleDeviceActor(FindingDeviceActivity.this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        bleDeviceActor.setCallback(this);
        iv_src = findViewById(R.id.iv_src);
        iv_find_dvs = findViewById(R.id.iv_find_dvs);

        Glide.with(this)
                .load(R.raw.searching)
                .into(iv_src)
                .onStart();

        Glide.with(this)
                .load(R.raw.finding_device)
                .into(iv_find_dvs)
                .onStart();
        today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        dateToStr = format.format(today);
        registerReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkselfPermission()) {
            //buildGoogleApiClient();
            startTimer();
            checkConnectionState();
            bleDeviceActor.setCallback(this);
            startScanUi();

        }
    }

    private void goToNextActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                devicesDialog devicesDialog = new devicesDialog();
                devicesDialog.showDialog(FindingDeviceActivity.this,deviceModelArrayList);
            }
        }, 3000);
    }

    private boolean checkselfPermission(){
        if (CheckSelfPermission.isBluetoothOn(FindingDeviceActivity.this)){
            if (CheckSelfPermission.isLocationOn(FindingDeviceActivity.this)){
                if(checkLocationPermission()){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkLocationPermission(){
        if (isFirstLocationPermission){
            isFirstLocationPermission = false;
            return CheckSelfPermission.checkLocationPermission(FindingDeviceActivity.this);
        }else {
            return CheckSelfPermission.checkLocationPermissionRetional(FindingDeviceActivity.this);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void checkConnectionState() {
        if (deviceModelArrayList != null && deviceModelArrayList.size() > connectPosition) {
            ConnectDeviceModel connectedModel = deviceModelArrayList.get(connectPosition);
            if (!AppMethods.isDeviceConnected(this, connectedModel.macAddress)) {
                deviceModelArrayList.remove(connectPosition);
                updateList();
            }
        }

        if (GlobalApplication.connectedModel == null) {
            return;
        }

        if (deviceModelArrayList.size() == 0 && AppMethods.isDeviceConnected(this, GlobalApplication.connectedModel.macAddress)) {
            connectPosition = 0;
            deviceModelArrayList.add(GlobalApplication.connectedModel);
            updateList();
        }
    }

    private void startScanUi() {

        ConnectDeviceModel connectedModel = null;
        if (deviceModelArrayList != null && deviceModelArrayList.size() > connectPosition) {
            connectedModel = deviceModelArrayList.get(connectPosition);
        }
        deviceModelArrayList = new ArrayList<>();
        if (connectedModel != null && connectedModel.isConnected) {
            deviceModelArrayList.add(connectedModel);
            connectPosition = 0;
        }
        bleDeviceActor.startScan();
        if (CheckSelfPermission.isBluetoothOn(this)) {

            updateList();
        }

    }

    private void stopScanUi() {
       runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
        bleDeviceActor.stopScan();

    }

    @Override
    public void scanCallback(String deviceName, String macAddress, int Rssi, int TxPower, String isCoded) {

        if (!isContainScanArrayList(macAddress, isCoded)) {
            deviceName = isContainDeviceName(deviceName);
            deviceModelArrayList.add(new ConnectDeviceModel(deviceName, macAddress, Rssi , TxPower, isCoded, today, false));

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    updateList();
                    handler.postDelayed(this, 2000);
                }
            }, 2000);

        }
    }

    private String lastConnectedDeviveName = "";

    @Override
    public void connectionCallback(boolean isConnected, String status) {
        checkConnectionState();
        if (isConnected) {
            BleCharacteristic.WriteCharacteristic(this, AppMethods.convertStringToByte(AppConstants.cmd_batteryLevel));
            ConnectDeviceModel connectedModel = deviceModelArrayList.get(connectPosition);
            connectedModel.isConnected = true;
            lastConnectedDeviveName = connectedModel.deviceName;
            AppMethods.writeDataTostrem(this, "Connected to " + connectedModel.deviceName + ", Coded=" + connectedModel.isCoded);
            deviceModelArrayList.remove(connectPosition);
            ArrayList<ConnectDeviceModel> deviceModelArrayListNew = new ArrayList<>();
            deviceModelArrayListNew.add(connectedModel);
            GlobalApplication.connectedModel = connectedModel;
            for (ConnectDeviceModel model : deviceModelArrayList) {
                deviceModelArrayListNew.add(model);
            }
            deviceModelArrayList = deviceModelArrayListNew;
            updateList();
            connectPosition = 0;
            AppMethods.hideProgressDialog(this);
        }
        else {
            Log.d(AppConstants.TAG, "from disconnect click: " + isdisconnectClicked);

            if (connectPosition == 0) {
                if (!isdisconnectClicked) {
                    if (!status.equals("")) {
                        AppMethods.setAlertDialog(this, status, "");
                    } else {
                        AppMethods.setAlertDialog(this, getString(R.string.device_disconnected), "");
                    }
                    stopScanUi();
                } else {
                    isdisconnectClicked = false;
                }
            }
            if (!status.equals("")) {
//                AppMethods.writeDataTostrem(getActivity(), status);
            } else {
                AppMethods.writeDataTostrem(this, "Disconnected from " + lastConnectedDeviveName);
            }
            AppMethods.hideProgressDialog(this);

        }

    }

    @Override
    public void onCharacteristicChanged(byte[] data) {

        String dataString = AppMethods.convertByteToString(data).toLowerCase();
        if (dataString.contains(AppConstants.cmd_batteryLevel_reply)) {
            String[] dataarray = dataString.split("_");
            if (dataarray.length >= 2) {
                String batterylevel = dataarray[1];
                if (batterylevel.startsWith("-")) {
                    batterylevel = "0";
                }
                int batterylevelint = (int) Float.parseFloat(batterylevel);
                deviceModelArrayList.get(0).battery_level = batterylevelint + "";
                deviceModelArrayList.get(0).battery_level_time = System.currentTimeMillis();
                updateList();
            }
        } else if (dataString.contains(AppConstants.Accepted_Blink_res)) {
            updateList();
        }
    }

    private boolean isdisconnectClicked = false;

    @Override
    public void connectClickCallback(boolean isConnectClick) {

        if (isConnectClick) {
            isdisconnectClicked = false;
            stopScanUi();
        } else {
            isdisconnectClicked = true;
            startScanUi();
        }
    }

    @Override
    public void drawTrainDot(float x, float y) {

    }

    @Override
    public void bluetoothCallback(boolean isOn) {
        stopScanUi();
        deviceModelArrayList = new ArrayList<>();
        updateList();
    }

    private boolean isContainScanArrayList(String macAddress, String isCoded) {
        for (ConnectDeviceModel model : deviceModelArrayList) {
            if (model.macAddress.toUpperCase().equals(macAddress.toUpperCase())) {
                if (!model.isCoded.equals(isCoded)) {
                    model.isCoded = isCoded;
                    updateList();
                }
                return true;
            }
        }
        return false;
    }

    private synchronized String isContainDeviceName(String deviceName) {
        String updated = deviceName;
        for (ConnectDeviceModel model : deviceModelArrayList) {
            String name = model.deviceName;
            int count = 2;
            if (name.contains("#")) {
                String[] nameArray = name.split("#");
                if (nameArray.length > 1) {
                    name = nameArray[0].trim();
                    count = Integer.parseInt(nameArray[1].trim()) + 1;
                }

            }
            if (name.toUpperCase().equals(deviceName.toUpperCase())) {
                updated = name + " #" + count;
            }
        }
        return updated;
    }

    private void startTimer() {
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 10 * 60 * 1000);
    }

    private void stoptimertask() {
        try {
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        } catch (Exception e) {

        }
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if (deviceModelArrayList.size() > 0 && deviceModelArrayList.get(0).isConnected) {
                    if ((System.currentTimeMillis() - deviceModelArrayList.get(0).battery_level_time) > 10 * 60 * 1000) {
                        BleCharacteristic.WriteCharacteristic(FindingDeviceActivity.this, AppMethods.convertStringToByte(AppConstants.cmd_batteryLevel));
                    }
                }
            }

        };
    }

    @Override
    public void onPause() {
        super.onPause();
        stoptimertask();

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    private void updateList() {
        if(deviceModelArrayList.size()!=0){
            Log.e("List Updated=>",""+deviceModelArrayList.size() + deviceModelArrayList.get(0).deviceName);
            goToNextActivity();
        }
    }
    private void registerReceiver() {
        unregiasterReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
        registerReceiver(bluetoothReceiver, intentFilter);
    }
    private void unregiasterReceiver() {
        try {
            if (bluetoothReceiver != null) {
                unregisterReceiver(bluetoothReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state;

            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        try {
                            if (BleDeviceActor.bleCallback != null) {
                                BleDeviceActor.bleCallback.bluetoothCallback(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        CheckSelfPermission.isBluetoothOn(FindingDeviceActivity.this);
                    } else if (state == BluetoothAdapter.STATE_ON) {
                        reconnectDevice();
                    }
                    break;


                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    break;
            }
        }
    };

}