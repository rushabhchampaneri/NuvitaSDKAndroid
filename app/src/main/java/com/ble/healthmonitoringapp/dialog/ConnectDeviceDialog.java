package com.ble.healthmonitoringapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.activity.MainActivity;
import com.ble.healthmonitoringapp.adapter.DevicesAdapter;

public class ConnectDeviceDialog {
    Dialog dialog;
    private Context mContext;
    private LinearLayout ll_main;

    public void showDialog(Context context) {
        mContext = context;
        dialog = new Dialog(context, R.style.DialogAnimation);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_connect_device);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(true);
        dialog.show();

        ll_main = dialog.findViewById(R.id.ll_main);

        ll_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intentMain = new Intent(mContext, MainActivity.class);
                mContext.startActivity(intentMain);
            }
        });

    }
}
