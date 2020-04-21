package com.example.snakemessenger;

import com.google.firebase.Timestamp;

class Message {
    private String senderID;
    private String date;
    private String time;
    private String content;

    public Message() {
    }

    public Message(String senderID, String date, String time, String content) {
        this.senderID = senderID;
        this.date = date;
        this.time = time;
        this.content = content;
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
}
