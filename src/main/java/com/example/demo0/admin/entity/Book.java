package com.example.demo0.admin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "book",
        uniqueConstraints = @UniqueConstraint(columnNames = "Barcode"))
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookid")
    private Integer bookId;

    @Column(name = "barcode", nullable = false, length = 20)
    private String barcode;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // 正常 / 下架 / 借出

    @Column(name = "shelfid")
    private Integer shelfId;  // 外键，不建立关联对象

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn; // 外键，不建立对象

    // === Constructors ===
    public Book() {
    }

    public Book(String barcode, String status, Integer shelfId, String isbn) {
        this.barcode = barcode;
        this.status = status;
        this.shelfId = shelfId;
        this.isbn = isbn;
    }

    // === Getters & Setters ===
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getShelfId() { return shelfId; }
    public void setShelfId(Integer shelfId) { this.shelfId = shelfId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    // === toString ===
    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", barcode='" + barcode + '\'' +
                ", status='" + status + '\'' +
                ", shelfId=" + shelfId +
                ", isbn='" + isbn + '\'' +
                '}';
    }
}
