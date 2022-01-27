package com.jstyle.blesdk2025.model;

/**
 * Created by Administrator on 2018/1/16.
 */

public class Notifier extends SendData{
    public static final  int Data_Tel=0;
    public static final  int Data_WeChat=1;
    public static final  int Data_Sms=2;
    public static final  int Data_Facebook=3;
    public static final  int Data_Telegra=4;
    public static final  int Data_Twitter=7;
    public static final  int Data_Vk=8;
    public static final  int Data_WhatApp=9;
    public static final  int Data_Stop_Tel=0xff;
    int type;
    String info;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "Notifier{" +
                "type=" + type +
                ", info='" + info + '\'' +
                '}';
    }
}
