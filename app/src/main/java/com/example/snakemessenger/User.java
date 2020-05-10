package com.example.snakemessenger;

import java.util.List;

public class User {
    private String userID;
    private String name;
    private String email;
    private boolean picture;
    private String status;

    public User() {
    }

    public User(String userID, String name, String email, boolean picture, String status) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getPicture() {
        return picture;
    }

    public void setPicture(boolean picture) {
        this.picture = picture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
