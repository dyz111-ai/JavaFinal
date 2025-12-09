package com.example.demo0.reader.model;

import java.sql.Timestamp;

/**
 * 书单-图书关联模型，对应 Booklist_Book 表
 */
public class BooklistBook {
    private Integer booklistId;
    private String isbn;
    private Timestamp addTime;
    private String notes;

    // Getters and Setters
    public Integer getBooklistId() {
        return booklistId;
    }

    public void setBooklistId(Integer booklistId) {
        this.booklistId = booklistId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Timestamp getAddTime() {
        return addTime;
    }

    public void setAddTime(Timestamp addTime) {
        this.addTime = addTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}



