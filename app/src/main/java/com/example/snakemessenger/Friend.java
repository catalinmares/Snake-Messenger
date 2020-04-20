package com.example.snakemessenger;

class Friend {
    private String userID;

    public Friend() {
    }

    public Friend(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
