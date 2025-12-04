package com.example.demo0.book.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BookInfo {
    private String ISBN;
    private String Title;
    private String Author;
    private Integer Stock; // 库存（来自数据库 BookInfo.Stock）
    private List<String> Categories = new ArrayList<>();

    public BookInfo() {}

    public BookInfo(String ISBN, String title, String author, List<String> categories) {
        this.ISBN = ISBN;
        this.Title = title;
        this.Author = author;
        if (categories != null) this.Categories = new ArrayList<>(categories);
    }

    public String getISBN() { return ISBN; }
    public void setISBN(String ISBN) { this.ISBN = ISBN; }

    public String getTitle() { return Title; }
    public void setTitle(String title) { Title = title; }

    public String getAuthor() { return Author; }
    public void setAuthor(String author) { Author = author; }

    public Integer getStock() { return Stock; }
    public void setStock(Integer stock) { Stock = stock; }

    public List<String> getCategories() { return Collections.unmodifiableList(Categories); }
    public void setCategories(List<String> categories) {
        this.Categories = new ArrayList<>();
        if (categories != null) this.Categories.addAll(categories);
    }

    public String getCategoriesString() {
        return String.join("，", this.Categories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookInfo)) return false;
        BookInfo bookInfo = (BookInfo) o;
        return Objects.equals(ISBN, bookInfo.ISBN);
    }

    @Override
    public int hashCode() { return Objects.hash(ISBN); }
}