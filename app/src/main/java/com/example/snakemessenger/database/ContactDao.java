package com.example.snakemessenger;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contacts")
    List<Contact> getContacts();

    @Query("SELECT * FROM contacts")
    LiveData<List<Contact>> getAllContacts();

    @Query("SELECT * FROM contacts WHERE chat = 1")
    List<Contact> getChatContacts();

    @Query("SELECT * FROM contacts WHERE chat = 1")
    LiveData<List<Contact>> getLiveChatContacts();

    @Query("SELECT * FROM contacts WHERE phone LIKE :phone LIMIT 1")
    Contact findByPhone(String phone);

    @Query("SELECT * FROM contacts WHERE name LIKE :name LIMIT 1")
    Contact findByName(String name);

    @Query("SELECT * FROM contacts WHERE phone LIKE :phone LIMIT 1")
    LiveData<Contact> findChangedByPhone(String phone);

    @Query("SELECT * FROM contacts WHERE endpointID LIKE :endpointID LIMIT 1")
    Contact findById(String endpointID);

    @Query("DELETE FROM contacts")
    void deleteAllContacts();

    @Insert
    void addContact(Contact contact);

    @Update
    void updateContact(Contact contact);

    @Query("UPDATE contacts SET connected = 0 WHERE connected = 1")
    void disconnectContacts();

    @Delete
    void deleteContact(Contact contact);
}
