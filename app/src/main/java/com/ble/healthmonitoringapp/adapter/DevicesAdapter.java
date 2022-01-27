package com.ble.healthmonitoringapp.adapter;

import static com.ble.healthmonitoringapp.dialog.devicesDialog.Devicesdialog;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ble.healthmonitoringapp.R;
import com.ble.healthmonitoringapp.dialog.ConnectDeviceDialog;
import com.ble.healthmonitoringapp.model.ConnectDeviceModel;
import com.ble.healthmonitoringapp.utils.AppMethods;
import com.bumptech.glide.Glide;
import com.jstyle.blesdk2025.model.ExtendedBluetoothDevice;

import java.util.ArrayList;
import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.MyViewHolder> {

    private Context context;
    ArrayList<ExtendedBluetoothDevice> deviceList = new ArrayList<>();
    private int total=0;
    int filterRssi;

    public DevicesAdapter(Context context, ArrayList<ExtendedBluetoothDevice> deviceModelArrayList) {
        this.context = context;
        this.deviceList = deviceModelArrayList;
    }
    public void setDeviceList(ArrayList<ExtendedBluetoothDevice> deviceList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
        //getFilter().filter(filterName);
    }
    public void addBondDevice(List<ExtendedBluetoothDevice> list) {

        deviceList.addAll(list);
        notifyDataSetChanged();

    }

    public void addDevice(BluetoothDevice device, String name, int rssi) {
        ExtendedBluetoothDevice bluetoothDevice = findDevice(device);
        if (bluetoothDevice == null) {
            deviceList.add(new ExtendedBluetoothDevice(device, name, rssi));
        } else {
            bluetoothDevice.rssi = rssi;
        }
    }

    private ExtendedBluetoothDevice findDevice(final BluetoothDevice device) {
        for (final ExtendedBluetoothDevice mDevice : deviceList) {
            if (mDevice.matches(device)) return mDevice;
        }
        return null;
    }

    public BluetoothDevice getDevice(int position) {
        return deviceList.get(position).device;
    }

    public String getName(int position) {
        return deviceList.get(position).name;
    }

    public void clear() {
        deviceList.clear();
    }

    public void setFilterRssi(int rssi) {
        this.filterRssi = rssi;
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

        int rssi = deviceList.get(position).rssi;
        int txPower = deviceList.get(position).device.getBondState();
        double distance = AppMethods.getDistance(rssi, txPower);
        int RSSI = AppMethods.getStrength(rssi);
        total=deviceList.size();
        total = total -1;
        if(position==total){
            holder.view.setVisibility(View.GONE);
        }
        else {
            holder.view.setVisibility(View.VISIBLE);
        }
        holder.tv_name.setText(deviceList.get(position).device.getName());
        holder.tv_distance.setText("" + distance);
        if (RSSI < 40) {
            holder.view_one.setBackgroundResource(R.drawable.red);
            holder.view_two.setBackgroundResource(R.drawable.red);
            holder.view_three.setBackgroundResource(R.drawable.gry);
            holder.view_four.setBackgroundResource(R.drawable.gry);
            holder.view_five.setBackgroundResource(R.drawable.gry);
            holder.view_six.setBackgroundResource(R.drawable.gry);
        }
        else if (RSSI < 60) {
            holder.view_one.setBackgroundResource(R.drawable.green);
            holder.view_two.setBackgroundResource(R.drawable.green);
            holder.view_three.setBackgroundResource(R.drawable.green);
            holder.view_four.setBackgroundResource(R.drawable.gry);
            holder.view_five.setBackgroundResource(R.drawable.gry);
            holder.view_six.setBackgroundResource(R.drawable.gry);

        }
        else if (RSSI < 80) {
            holder.view_one.setBackgroundResource(R.drawable.green);
            holder.view_two.setBackgroundResource(R.drawable.green);
            holder.view_three.setBackgroundResource(R.drawable.green);
            holder.view_four.setBackgroundResource(R.drawable.green);
            holder.view_five.setBackgroundResource(R.drawable.gry);
            holder.view_six.setBackgroundResource(R.drawable.gry);
        }
        else {
            holder.view_one.setBackgroundResource(R.drawable.green);
            holder.view_two.setBackgroundResource(R.drawable.green);
            holder.view_three.setBackgroundResource(R.drawable.green);
            holder.view_four.setBackgroundResource(R.drawable.green);
            holder.view_five.setBackgroundResource(R.drawable.green);
            holder.view_six.setBackgroundResource(R.drawable.green);
        }
        holder.ll_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectDeviceDialog connectDeviceDialog = new ConnectDeviceDialog();
                connectDeviceDialog.showDialog(context);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View view, view_one, view_two, view_three, view_four, view_five, view_six;
        private LinearLayout ll_main;
        private TextView tv_name,tv_distance;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView.findViewById(R.id.view);
            view_one = itemView.findViewById(R.id.view_one);
            view_two = itemView.findViewById(R.id.view_two);
            view_three = itemView.findViewById(R.id.view_three);
            view_four = itemView.findViewById(R.id.view_four);
            view_five = itemView.findViewById(R.id.view_five);
            view_six = itemView.findViewById(R.id.view_six);
            ll_main = itemView.findViewById(R.id.ll_main);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_distance = itemView.findViewById(R.id.tv_distance);
        }
    }
}
