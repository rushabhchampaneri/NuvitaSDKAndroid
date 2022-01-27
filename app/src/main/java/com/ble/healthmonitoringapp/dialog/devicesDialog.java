package com.ble.healthmonitoringapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.adapter.DevicesAdapter;
import com.ble.healthmonitoringapp.model.ConnectDeviceModel;

import java.util.ArrayList;

public class devicesDialog {
    public static Dialog Devicesdialog;
    private Context mContext;
    private RecyclerView rcv_devices;
    private boolean isShow=false;

    public void showDialog(Context context, ArrayList<ConnectDeviceModel> deviceModelArrayList) {
        mContext = context;
        Devicesdialog = new Dialog(mContext);
        Devicesdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        Devicesdialog.getWindow().setGravity(Gravity.BOTTOM);
        Devicesdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Devicesdialog.setContentView(R.layout.dialog_devices);
        Devicesdialog.setCancelable(true);
        Devicesdialog.setCanceledOnTouchOutside(true);
        Devicesdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Devicesdialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        rcv_devices = Devicesdialog.findViewById(R.id.rcv_devices);

        if(!isShow){
            Devicesdialog.show();
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            rcv_devices.setLayoutManager(mLayoutManager);
            DevicesAdapter devicesAdapter = new DevicesAdapter(mContext,deviceModelArrayList);
            rcv_devices.setAdapter(devicesAdapter);
        }
        else {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            rcv_devices.setLayoutManager(mLayoutManager);
            DevicesAdapter devicesAdapter = new DevicesAdapter(mContext,deviceModelArrayList);
            rcv_devices.setAdapter(devicesAdapter);
        }




    }
}
