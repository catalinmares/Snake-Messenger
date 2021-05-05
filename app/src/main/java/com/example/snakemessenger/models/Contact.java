package com.example.snakemessenger.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts")
public class Contact {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "deviceID")
    private String deviceID;

    @ColumnInfo(name = "endpointID")
    private String endpointID;

    @ColumnInfo(name = "connected")
    private boolean connected;

    @ColumnInfo(name = "last_active")
    private long lastActive;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "photoUri")
    private String photoUri;

    @ColumnInfo(name = "chat")
    private boolean chat;

    @ColumnInfo(name = "last_message_timestamp")
    private long lastMessageTimestamp;

    @ColumnInfo(name = "saved")
    private boolean saved;

    @ColumnInfo(name = "nearby")
    private boolean nearby;

    @Ignore
    public Contact() {
    }

    public Contact(int id, String name, String deviceID, String endpointID, boolean connected, long lastActive,
                   String description, String photoUri, boolean chat, long lastMessageTimestamp, boolean saved, boolean nearby) {
        this.id = id;
        this.name = name;
        this.deviceID = deviceID;
        this.endpointID = endpointID;
        this.connected = connected;
        this.lastActive = lastActive;
        this.description = description;
        this.photoUri = photoUri;
        this.chat = chat;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.saved = saved;
        this.nearby = nearby;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getEndpointID() {
        return endpointID;
    }

    public void setEndpointID(String endpointID) {
        this.endpointID = endpointID;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public boolean isChat() {
        return chat;
    }

    public void setChat(boolean chat) {
        this.chat = chat;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public boolean isNearby() {
        return nearby;
    }

    public void setNearby(boolean nearby) {
        this.nearby = nearby;
    }
}
