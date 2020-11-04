package com.example.snakemessenger.connectionRequests;

import java.util.Date;

public class ConnectionRequest {
    private String senderID;
    private String senderName;
    private String timestamp;

    public ConnectionRequest(String senderID, String senderName, String timestamp) {
        this.senderID = senderID;
        this.senderName = senderName;
        this.timestamp = timestamp;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
