package com.example.snakemessenger;

import java.util.List;

public class User {
    private String userID;
    private String name;
    private String email;
    private String picture;
    private String status;
    private List<String> friends;

    public User() {
    }

    public User(String userID, String name, String email, String picture, String status, List<String> friends) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.status = status;
        this.friends = friends;
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}
