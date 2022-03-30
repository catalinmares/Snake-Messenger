package com.example.snakemessenger.models;

import androidx.annotation.Nullable;

public class ImagePart implements Comparable<ImagePart> {
    private int partNo;
    private int size;
    private byte[] content;

    public ImagePart(int partNo, int size, byte[] content) {
        this.partNo = partNo;
        this.size = size;
        this.content = content;
    }

    public int getPartNo() {
        return partNo;
    }

    public void setPartNo(int partNo) {
        this.partNo = partNo;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ImagePart)) {
            return false;
        }

        ImagePart other = (ImagePart) obj;

        return this.getPartNo() == other.getPartNo();
    }

    @Override
    public int compareTo(ImagePart o) {
        return this.partNo - o.partNo;
    }
}
