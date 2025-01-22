package com.findr.findr.entity;


import android.graphics.Point;

public class User {
    private Long id;
    private String username;
    private String password;
    private int age;
    private String email;
    private Point location = new Point(0,0);
    private String name;

    public User(String username, String password, Point location, int age){
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
}
