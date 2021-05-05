package com.example.snakemessenger.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.snakemessenger.models.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages WHERE source = :contactId AND destination = :currentDeviceId OR source = :currentDeviceId AND destination = :contactId ORDER BY timestamp")
    List<Message> getMessages(String currentDeviceId, String contactId);

    @Query("SELECT * FROM messages WHERE status = 2 LIMIT 25")
    List<Message> getLastReceivedMessages();

    @Query("SELECT * FROM messages WHERE source = :contactId AND destination = :currentDeviceId OR source = :currentDeviceId AND destination = :contactId ORDER BY timestamp")
    LiveData<List<Message>> getLiveMessages(String currentDeviceId, String contactId);

    @Query("SELECT * FROM messages WHERE destination = :contactId AND status = 0 OR status = 3")
    List<Message> getUndeliveredMessages(String contactId);

    @Query("SELECT * FROM messages WHERE payloadId = :payloadId LIMIT 1")
    Message getMessageByPayloadId(long payloadId);

    @Query("SELECT * FROM messages WHERE (source = :contactId AND destination = :currentDeviceId) OR (source = :currentDeviceId AND destination = :contactId) ORDER BY timestamp DESC LIMIT 1")
    Message getLastMessage(String currentDeviceId, String contactId);

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 1")
    LiveData<Message> getLiveMessage();

    @Query("SELECT * FROM messages WHERE source = :contactId OR destination = :contactId ORDER BY timestamp DESC LIMIT 1")
    LiveData<Message> getLastLiveMessage(String contactId);

    @Query("SELECT * FROM messages WHERE source != :currentDeviceId AND destination = :contactId ORDER BY timestamp")
    List<Message> getDataMemory(String currentDeviceId, String contactId);

    @Query("SELECT * FROM messages WHERE source = :currentDeviceId AND destination = :contactId AND status = 0 ORDER BY timestamp")
    List<Message> getOwnMessages(String currentDeviceId, String contactId);

    @Query("SELECT * FROM messages WHERE messageId = :messageId LIMIT 1")
    Message findByMessageId(String messageId);

    @Query("DELETE FROM messages WHERE status = 3 AND timestamp < (:now - 3*24*60*60*1000)")
    void deleteOldUndeliveredMessages(long now);

    @Insert
    void addMessage(Message message);

    @Update
    void updateMessage(Message message);

    @Delete
    void deleteMessage(Message message);
}
