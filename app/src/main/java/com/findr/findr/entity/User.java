package com.findr.findr.entity;


import android.graphics.Point;
import android.location.Location;

import java.util.List;

public class User {
    private Long id;
    private String username;
    private String password;
    private int age;
    private String email;
    private LocationData location = new LocationData(0,0);
    private String name;
    private String description;

    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    public User(String name, int age, String email, String username, String password, String description){
        this.name = name;
        this.age = age;
        this.email = email;
        this.username = username;
        this.password = password;
        this.description = description;

    }
    public User(String username, String password, LocationData location, int age){
        this.password = password;
        this.username = username;
        this.location = location;
        this.age = age;
    }
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

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }

    @Override
    public String toString(){
        return "Name: " + username +
                "\nID: " + id +
                "\nPassword: " + password +
                "Age: " + age +
                "Email: " + email +
                "Name: " + name +
                "\nLocation: " + (location != null ? location : "null");
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
