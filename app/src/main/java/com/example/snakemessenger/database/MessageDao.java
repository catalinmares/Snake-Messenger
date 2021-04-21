package com.example.snakemessenger.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messages WHERE to_from = :contact ORDER BY timestamp")
    List<Message> getMessages(String contact);

    @Query("SELECT * FROM messages WHERE to_from = :contact ORDER BY timestamp")
    LiveData<List<Message>> getLiveMessages(String contact);

    @Query("SELECT * FROM messages WHERE to_from =:contact AND status = 0")
    List<Message> getUndeliveredMessages(String contact);

    @Query("SELECT * FROM messages WHERE payloadId = :payloadId LIMIT 1")
    Message getMessageByPayloadId(long payloadId);

    @Query("SELECT * FROM messages WHERE to_from = :contact ORDER BY timestamp DESC LIMIT 1")
    Message getLastMessage(String contact);

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 1")
    LiveData<Message> getLiveMessage();

    @Query("SELECT * FROM messages WHERE to_from = :contact ORDER BY timestamp DESC LIMIT 1")
    LiveData<Message> getLastLiveMessage(String contact);

    @Query("DELETE FROM messages")
    void deleteAllMessages();

    @Insert
    void addMessage(Message message);

    @Update
    void updateMessage(Message message);

    @Delete
    void deleteMessage(Message message);
}
