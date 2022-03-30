package com.example.snakemessenger.models;

import java.util.ArrayList;
import java.util.List;

public class ImageMessage {
    private String messageId;
    private long payloadId;
    private String sourceId;
    private String destinationId;
    private long timestamp;
    private int totalSize;
    private final List<ImagePart> parts;

    public ImageMessage(String messageId, String sourceId, String destinationId, long timestamp, int totalSize) {
        this.messageId = messageId;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.timestamp = timestamp;
        this.totalSize = totalSize;
        this.parts = new ArrayList<>();
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(long payloadId) {
        this.payloadId = payloadId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getCurrentSize() {
        int size = 0;

        for (ImagePart part : parts) {
            size += part.getSize();
        }

        return size;
    }

    public List<ImagePart> getParts() {
        return parts;
    }

    public void addPart(ImagePart part) {
        this.parts.add(part);
    }
}
