package com.example.snakemessenger.groups;
import java.util.List;

class Group {
    private String name;
    private String adminID;
    private List<String> users;

    public Group() {

    }

    public Group(String name, String adminID, List<String> users) {
        this.name = name;
        this.adminID = adminID;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
