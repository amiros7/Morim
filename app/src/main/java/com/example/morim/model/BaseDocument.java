package com.example.morim.model;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

public class BaseDocument {

    @PrimaryKey
    @NonNull
    protected String id;
    protected Long updatedAt;

    public BaseDocument(String id) {
        this.id = id;
        this.updatedAt = System.currentTimeMillis();
    }

    public BaseDocument() {
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }
}
