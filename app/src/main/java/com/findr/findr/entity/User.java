package com.findr.findr.entity;


import android.graphics.Point;

public class User {
    private Long id;
    private String username;
    private String password;
    private Point location;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    @Override
    public String toString(){
        return "Name: " + username +
                "\nID: " + id +
                "\nPassword: " + password +
                "\nLocation: " + location.toString();

    }
}
