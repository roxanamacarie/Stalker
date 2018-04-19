package com.example.macarie.stalker;

import java.io.Serializable;

/**
 * Created by macarie on 19/04/2018.
 */

public class User implements Serializable {

    static final long serialVersionUID = 1L;

    String username;
    String latitude;
    String longitude;
    String day;
    String hour;

    public User() {
    }

    public User(String username, String latitude, String longitude, String day, String hour) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.day = day;
        this.hour = hour;
    }

    public String getUsername() {
        return username;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDay() {
        return day;
    }

    public String getHour() {
        return hour;
    }
}
