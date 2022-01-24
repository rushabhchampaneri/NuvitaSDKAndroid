package com.ble.healthmonitoringapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.adapter.DevicesAdapter;

public class devicesDialog {
    public static Dialog Devicesdialog;
    private Context mContext;
    private RecyclerView rcv_devices;

    public void showDialog(Context context) {
        mContext = context;
        Devicesdialog = new Dialog(context, R.style.DialogAnimation);
        Devicesdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Devicesdialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Devicesdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Devicesdialog.setContentView(R.layout.dialog_devices);
        Devicesdialog.getWindow().setGravity(Gravity.BOTTOM);
        Devicesdialog.setCancelable(true);
        Devicesdialog.show();

        rcv_devices = Devicesdialog.findViewById(R.id.rcv_devices);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        rcv_devices.setLayoutManager(mLayoutManager);
        DevicesAdapter devicesAdapter = new DevicesAdapter(mContext);
        rcv_devices.setAdapter(devicesAdapter);

    }
}
