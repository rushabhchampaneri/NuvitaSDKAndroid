package com.ble.healthmonitoringapp.model;

public class ValuesModel {
    int value=0;
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

    public ValuesModel(int value, String date) {
        this.value = value;
        this.date = date;
    }
}
