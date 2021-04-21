package com.example.snakemessenger.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "messages")
public
class Message {
    @Ignore
    public static final int SENT = 0;

    @Ignore
    public static final int DELIVERED = 1;

    @Ignore
    public static final int RECEIVED = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "payloadId")
    private long payloadId;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "to_from")
    private String toFrom;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "status")
    private int status;

    @Ignore
    public Message() {
    }

    public Message(int id, long payloadId, int type, String toFrom, String content, Date timestamp, int status) {
        this.id = id;
        this.payloadId = payloadId;
        this.type = type;
        this.toFrom = toFrom;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getToFrom() {
        return toFrom;
    }

    public void setToFrom(String toFrom) {
        this.toFrom = toFrom;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
