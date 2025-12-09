package com.example.demo0.book.model;

/**
 * 图书分类关联详情模型
 * 包含图书信息和分类信息
 */
public class BookCategoryDetail {
    private String isbn;
    private String title;
    private String author;
    private String categoryId;
    private String categoryName;
    private String categoryPath; // 分类完整路径，如 "文学 / 小说 / 现代小说"
    private String relationNote;

    public BookCategoryDetail() {}

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(String categoryPath) {
        this.categoryPath = categoryPath;
    }

    public String getRelationNote() {
        return relationNote;
    }

    public void setRelationNote(String relationNote) {
        this.relationNote = relationNote;
    }
}

