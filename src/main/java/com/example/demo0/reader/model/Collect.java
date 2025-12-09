package com.example.demo0.reader.model;

import java.sql.Timestamp;

/**
 * 书单收藏模型，对应 Collect 表
 */
public class Collect {
    private Integer booklistId;
    private Integer readerId;
    private Timestamp favoriteTime;
    private String notes;

    // Getters and Setters
    public Integer getBooklistId() {
        return booklistId;
    }

    public void setBooklistId(Integer booklistId) {
        this.booklistId = booklistId;
    }

    public Integer getReaderId() {
        return readerId;
    }

    public void setReaderId(Integer readerId) {
        this.readerId = readerId;
    }

    public Timestamp getFavoriteTime() {
        return favoriteTime;
    }

    public void setFavoriteTime(Timestamp favoriteTime) {
        this.favoriteTime = favoriteTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}



