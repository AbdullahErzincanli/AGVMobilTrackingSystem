package com.example.mobilagv_v1.MODEL;

import android.content.Intent;

public class QRAGV {

    private String Name, IP, RobotLength, robotwidth;
    private int Length, Width, Port, OdomLength, OdomWidth;
    public static final int isPortEmpty = -1;
    public static final String isIPEmpty = "-1";


    public QRAGV() {

        Name = isIPEmpty;
        IP = isIPEmpty;
        RobotLength= isIPEmpty;
        robotwidth= isIPEmpty;
        Length = isPortEmpty;
        Width = isPortEmpty;
        Port = isPortEmpty;
    }


    public int getOdomLength() {
        return OdomLength;
    }

    public void setOdomLength(String odomLength) {
        OdomLength = Integer.parseInt(odomLength);
    }

    public int getOdomWidth() {
        return OdomWidth;
    }

    public void setOdomWidth(String odomWidth) {
        OdomWidth = Integer.parseInt(odomWidth);
    }

    public Integer getPort() {
        return Port;
    }

    public void setPort(int port) {
        Port = port;
    }

    public void setPort(String port) {
        Port = Integer.parseInt(port);
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getRobotLength() {
        return RobotLength;
    }

    public void setRobotLength(String robotLength) {
        RobotLength = robotLength;
        Length = Integer.parseInt(robotLength);
    }

    public String getRobotwidth() {
        return robotwidth;
    }

    public void setRobotwidth(String robotwidth) {
        this.robotwidth = robotwidth;
        Width = Integer.parseInt(robotwidth);
    }

    public int getLength() {
        return Length;
    }

    public int getWidth() {
        return Width;
    }
}
