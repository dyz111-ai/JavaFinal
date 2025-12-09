package com.example.demo0.book.model;

/**
 * 图书分类关联模型
 * 对应数据库 Book_Classify 表
 */
public class BookCategory {
    private String isbn;
    private String categoryId;
    private String relationNote;

    public BookCategory() {}

    public BookCategory(String isbn, String categoryId, String relationNote) {
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.relationNote = relationNote;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getRelationNote() {
        return relationNote;
    }

    public void setRelationNote(String relationNote) {
        this.relationNote = relationNote;
    }
}

