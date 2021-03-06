package com.ble.healthmonitoringapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.activity.MainActivity;

public class ConnectDeviceDialog {
    Dialog dialog;
    private Context mContext;
    private LinearLayout ll_main;
    private TextView tvName;

    public void showDialog(Context context,String name) {
        mContext = context;
        dialog = new Dialog(mContext);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_connect_device);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        tvName=dialog.findViewById(R.id.tvName);
        ll_main = dialog.findViewById(R.id.ll_main);
        tvName.setText(name+" "+context.getString(R.string.ecg_bluetooth01_device_connected_succesfully));
        new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               devicesDialog.Devicesdialog.dismiss();
               dialog.dismiss();
               Intent intentMain = new Intent(mContext, MainActivity.class);
               mContext.startActivity(intentMain);
           }
       },1500);

    }
}
