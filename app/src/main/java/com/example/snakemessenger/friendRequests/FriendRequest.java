package com.example.snakemessenger.friendRequests;

import com.google.firebase.Timestamp;

class FriendRequest {
    private String sender;
    private String receiver;
    private Timestamp timestamp;

    public FriendRequest() {
    }

    public FriendRequest(String sender, String receiver, Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
