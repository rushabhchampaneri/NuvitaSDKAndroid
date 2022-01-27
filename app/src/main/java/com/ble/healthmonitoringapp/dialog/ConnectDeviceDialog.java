package com.ble.healthmonitoringapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.activity.MainActivity;

public class ConnectDeviceDialog {
    Dialog dialog;
    private Context mContext;
    private LinearLayout ll_main;

    public void showDialog(Context context) {
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
        ll_main = dialog.findViewById(R.id.ll_main);
        ll_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devicesDialog.Devicesdialog.dismiss();
                dialog.dismiss();
                Intent intentMain = new Intent(mContext, MainActivity.class);
                mContext.startActivity(intentMain);
            }
        });

    }
}
