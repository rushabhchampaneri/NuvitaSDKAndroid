package com.ble.healthmonitoringapp.utils;

import android.content.Context;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AppConstants {
    public static final int REQUEST_LOCATION_PERMISSION = 01;
    public static final int REQUEST_STORAGE_PERMISSION = 02;
    public static final int REQUEST_ENABLE_BLUETOOTH = 03;
    public static final int MY_MARSHMELLO_PERMISSION = 04;
    public static final int REQUEST_ENABLE_LOCATION = 05;
    public static String CONNECTED_MAC_ADDRESS_PREF = "connect_mac";
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    public final static String default_notification_channel_id = "default" ;
    public static Context context;
    public static LatLng DeviceLocation = new LatLng(0.0,0.0);
    public static String DeviceName = "devicename";
    public static String DeviceDistance = "devicedistance";
    public static String DeviceStrength = "devicedistance";
    public static String RECENT_PREF = "recentlist";
    public static final String TAG = "ble==>";
    public static String NAME="name";
    public static String TYPE="type";
    public static LatLng LOCATION;
    public static String DISTANCE="distance";
    public static int RSSI;

    public static final String UART_SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final String WRITE_CHAR_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final String NOTIFY_CHAR_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    public static final String DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    public static final String Accepted_Blink_res = "accepted_blink"; //Accepted_Blink
    public static final String getMetroLogyUrl = "http://api.openweathermap.org/data/2.5/weather?";
    public static final String appIdForWebSrevice = "&appid=4e65499af8ea8e68afb146dce1fbeb5f";
    public static final String cmd_batteryLevel = "cmd_BatteryLevel";
    public static final String cmd_batteryLevel_reply = "batterylevel_";   //BatteryLevel_num

    private final static AtomicInteger c = new AtomicInteger(0);

    public static int getID() {
        return c.incrementAndGet();
    }

}
