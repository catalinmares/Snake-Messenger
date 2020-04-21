package com.example.snakemessenger.chats;

public class Chat {
    private String userID;

    public Chat() {
    }

    public Chat(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
