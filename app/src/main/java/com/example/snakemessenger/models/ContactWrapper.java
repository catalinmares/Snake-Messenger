package com.example.snakemessenger.models;

public class ContactWrapper {
    private Contact contact;
    private boolean selected;

    public ContactWrapper(Contact contact, boolean selected) {
        this.contact = contact;
        this.selected = selected;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
