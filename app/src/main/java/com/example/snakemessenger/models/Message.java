package com.example.snakemessenger.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages")
public
class Message {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "messageId")
    private String messageId;

    @ColumnInfo(name = "payloadId")
    private long payloadId;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "source")
    private String source;

    @ColumnInfo(name = "destination")
    private String destination;

    @ColumnInfo(name = "contentType")
    private int contentType;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "totalSize")
    private long totalSize;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "timesSent")
    private int timesSent;

    @ColumnInfo(name = "status")
    private int status;

    @Ignore
    public Message() {
    }

    public Message(int id, String messageId, long payloadId, int type, String source, String destination, int contentType, String content, long totalSize, long timestamp, int timesSent, int status) {
        this.id = id;
        this.messageId = messageId;
        this.payloadId = payloadId;
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.contentType = contentType;
        this.content = content;
        this.totalSize = totalSize;
        this.timestamp = timestamp;
        this.timesSent = timesSent;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public long getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(long payloadId) {
        this.payloadId = payloadId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimesSent() {
        return timesSent;
    }

    public void setTimesSent(int timesSent) {
        this.timesSent = timesSent;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
