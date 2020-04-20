package com.example.snakemessenger.friendRequests;

class FriendRequest {
    private String userID;

    public FriendRequest() {
    }

    public FriendRequest(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
