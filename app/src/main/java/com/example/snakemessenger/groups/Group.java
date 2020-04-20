package com.example.snakemessenger.groups;
import java.util.List;

class Group {
    private String name;
    private String adminID;
    private String description;
    private String picture;
    private List<String> users;

    public Group() {

    }

    public Group(String name, String adminID, String description, String picture, List<String> users) {
        this.name = name;
        this.adminID = adminID;
        this.description = description;
        this.picture = picture;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
