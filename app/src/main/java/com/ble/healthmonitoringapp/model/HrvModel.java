package com.ble.healthmonitoringapp.model;

public class HrvModel {
    int hrv,stress,lowBp,highBp;
    String date;

    public int getStress() {
        return stress;
    }

    public void setStress(int stress) {
        this.stress = stress;
    }

    public int getLowBp() {
        return lowBp;
    }

    public void setLowBp(int lowBp) {
        this.lowBp = lowBp;
    }

    public int getHighBp() {
        return highBp;
    }

    public void setHighBp(int highBp) {
        this.highBp = highBp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHrv() {
        return hrv;
    }

    public void setHrv(int hrv) {
        this.hrv = hrv;
    }

    public HrvModel(int hrv, int stress, int lowBp, int highBp, String date) {
        this.hrv = hrv;
        this.stress = stress;
        this.lowBp = lowBp;
        this.highBp = highBp;
        this.date = date;
    }
}
