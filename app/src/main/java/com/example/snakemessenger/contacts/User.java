package com.example.snakemessenger;

public class User {
    private String endpointID;
    private String phone;

    public User(String endpointID, String phone) {
        this.endpointID = endpointID;
        this.phone = phone;
    }

    public String getEndpointID() {
        return endpointID;
    }

    public void setEndpointID(String endpointID) {
        this.endpointID = endpointID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
