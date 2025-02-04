package com.findr.findr.entity;

import android.graphics.Point;

public class Post {
    private Long id;
    private String description;
    private String author;
    private String photoPath;
    private Point location;
    private Long likes;

    public Post(String description, String author, Point location) {
        this.id = id;
        this.description = description;
        this.author = author;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
    @Override
    public String toString(){
        return "EntityDetails {" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", location=" + (location != null ? location.toString() : "null") +
                '}';
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }
}
