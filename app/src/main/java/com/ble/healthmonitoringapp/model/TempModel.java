package com.ble.healthmonitoringapp.model;

public class TempModel
{
    Float value;
    String date;

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public TempModel(Float value, String date) {
        this.value = value;
        this.date = date;
    }
}
