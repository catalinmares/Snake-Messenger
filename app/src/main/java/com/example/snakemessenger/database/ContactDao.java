package com.example.snakemessenger.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.snakemessenger.models.Contact;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contacts WHERE saved = 1 ORDER BY name")
    List<Contact> getContacts();

    @Query("SELECT * FROM contacts WHERE saved = 1 ORDER BY name")
    LiveData<List<Contact>> getAllContacts();

    @Query("SELECT * FROM contacts WHERE nearby = 1")
    List<Contact> getNearbyContacts();

    @Query("SELECT * FROM contacts WHERE nearby = 1")
    LiveData<List<Contact>> getLiveNearbyContacts();

    @Query("SELECT * FROM contacts WHERE chat = 1 ORDER BY last_message_timestamp DESC")
    List<Contact> getChatContacts();

    @Query("SELECT * FROM contacts WHERE chat = 1 ORDER BY last_message_timestamp DESC")
    LiveData<List<Contact>> getLiveChatContacts();

    @Query("SELECT * FROM contacts WHERE name LIKE :name")
    List<Contact> getMatchingContacts(String name);

    @Query("SELECT * FROM contacts ORDER BY last_active DESC LIMIT 25")
    List<Contact> getLastInteractions();

    @Query("SELECT * FROM contacts WHERE deviceID LIKE :deviceId LIMIT 1")
    Contact findByDeviceId(String deviceId);

    @Query("SELECT * FROM contacts WHERE name LIKE :name LIMIT 1")
    Contact findByName(String name);

    @Query("SELECT * FROM contacts WHERE deviceID LIKE :deviceId LIMIT 1")
    LiveData<Contact> findChangedByDeviceId(String deviceId);

    @Query("SELECT * FROM contacts WHERE endpointID LIKE :endpointID LIMIT 1")
    Contact findById(String endpointID);

    @Query("DELETE FROM contacts WHERE saved = 0 AND (last_active < (:now - 7*24*60*60*1000))")
    void deleteOldContacts(long now);

    @Insert
    void addContact(Contact contact);

    @Update
    void updateContact(Contact contact);

    @Delete
    void deleteContact(Contact contact);
}
