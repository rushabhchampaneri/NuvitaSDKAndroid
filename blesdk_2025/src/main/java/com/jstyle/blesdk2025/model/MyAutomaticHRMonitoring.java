package com.jstyle.blesdk2025.model;

/**
 * Created by Administrator on 2018/1/16.
 */

public class MyAutomaticHRMonitoring extends SendData{
    int open;//1 开启整个时间段都测量 2时间段内间隔测量 0关闭
    int startHour;
    int startMinute;
    int endHour;
    int endMinute;
    int week;
    int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    @Override
    public String toString() {
        return "MyAutomaticHRMonitoring{" +
                "open=" + open +
                ", startHour=" + startHour +
                ", startMinute=" + startMinute +
                ", endHour=" + endHour +
                ", endMinute=" + endMinute +
                ", week=" + week +
                ", time=" + time +
                '}';
    }
}
