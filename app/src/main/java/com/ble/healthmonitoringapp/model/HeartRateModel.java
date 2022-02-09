package com.ble.healthmonitoringapp.model;

public class HeartRateModel {
    int value;
    String date;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public HeartRateModel(int value, String date) {
        this.value = value;
        this.date = date;
    }
}
