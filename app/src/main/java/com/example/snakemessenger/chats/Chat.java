package com.example.snakemessenger.chats;

public class Chat {
    private String friendName;
    private String friendPhoto;

    public Chat() {

    }

    public Chat(String friendName, String friendPhoto) {
        this.friendName = friendName;
        this.friendPhoto = friendPhoto;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendPhoto() {
        return friendPhoto;
    }

    public void setFriendPhoto(String friendPhoto) {
        this.friendPhoto = friendPhoto;
    }
}
