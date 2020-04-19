package com.example.snakemessenger;

import com.google.firebase.Timestamp;

class GroupMessage {
    private String senderID;
    private String date;
    private String time;
    private String content;
    private Timestamp timestamp;

    public GroupMessage() {

    }

    public GroupMessage(String senderID, String date, String time, String content, Timestamp timestamp) {
        this.senderID = senderID;
        this.date = date;
        this.time = time;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
