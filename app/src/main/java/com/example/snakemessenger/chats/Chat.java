package com.example.snakemessenger.chats;

import java.util.List;

public class Chat {
    private List<String> users;

    public Chat() {
    }

    public Chat(List<String> users) {
        this.users = users;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
