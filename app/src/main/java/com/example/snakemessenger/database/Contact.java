package com.example.snakemessenger;

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

    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "endpointID")
    private String endpointID;

    @ColumnInfo(name = "connected")
    private boolean connected;

    @ColumnInfo(name = "last_active")
    private String lastActive;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "photoUri")
    private String photoUri;

    @ColumnInfo(name = "chat")
    private boolean chat;

    @Ignore
    public Contact() {
    }

    public Contact(int id, String name, String phone, String endpointID, boolean connected,
                   String lastActive, String description, String photoUri, boolean chat) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.endpointID = endpointID;
        this.connected = connected;
        this.lastActive = lastActive;
        this.description = description;
        this.photoUri = photoUri;
        this.chat = chat;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getLastActive() {
        return lastActive;
    }

    public void setLastActive(String lastActive) {
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
}
