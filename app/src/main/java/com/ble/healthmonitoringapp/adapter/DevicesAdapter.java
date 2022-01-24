package com.ble.healthmonitoringapp.adapter;

import static com.ble.healthmonitoringapp.dialog.devicesDialog.Devicesdialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.dialog.ConnectDeviceDialog;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.MyViewHolder>{

    private Context context;

    public DevicesAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.raw_devices, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if(position==5){
            holder.view.setVisibility(View.GONE);
        }
        holder.ll_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Devicesdialog.dismiss();
                ConnectDeviceDialog connectDeviceDialog = new ConnectDeviceDialog();
                connectDeviceDialog.showDialog(context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 6;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private LinearLayout ll_main;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.view);
            ll_main = itemView.findViewById(R.id.ll_main);
        }
    }
}
