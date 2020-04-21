package com.example.snakemessenger.friendRequests;

import java.sql.Timestamp;

class FriendRequest {
    private String userID;
    private String time;
    private String date;
    private String status;

    public FriendRequest() {
    }

    public FriendRequest(String userID, String time, String date, String status) {
        this.userID = userID;
        this.time = time;
        this.date = date;
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
