package com.example.demo0.admin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bookinfo")
public class Bookinfo {

    @Id
    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 40)
    private String author;

    @Column(nullable = false)
    private Integer stock;

    // === Constructors ===
    public Bookinfo() {
    }

    public Bookinfo(String isbn, String title, String author, Integer stock) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.stock = stock;
    }

    // === Getters & Setters ===
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // === toString ===
    @Override
    public String toString() {
        return "Bookinfo{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", stock=" + stock +
                '}';
    }
}
