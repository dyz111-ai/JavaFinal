package com.example.demo0.reader.model;

import java.time.OffsetDateTime;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.math.BigDecimal;

/**
 * 借阅记录详情DTO（包含图书和读者信息）
 */
public class BorrowRecordDetail {
    private Integer borrowRecordId;
    private String bookId;
    private String isbn;
    private String bookTitle;
    private String bookAuthor;
    private String readerId;
    private String readerName;
    private OffsetDateTime borrowTime;
    private OffsetDateTime returnTime;
    private BigDecimal overdueFine;

    // Getters and Setters
    public Integer getBorrowRecordId() {
        return borrowRecordId;
    }

    public void setBorrowRecordId(Integer borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public OffsetDateTime getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(OffsetDateTime borrowTime) {
        this.borrowTime = borrowTime;
    }

    public void setBorrowTime(Timestamp ts) {
        if (ts == null) {
            this.borrowTime = null;
            return;
        }
        this.borrowTime = ts.toInstant().atOffset(ZoneOffset.UTC);
    }

    public OffsetDateTime getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(OffsetDateTime returnTime) {
        this.returnTime = returnTime;
    }

    public void setReturnTime(Timestamp ts) {
        if (ts == null) {
            this.returnTime = null;
            return;
        }
        this.returnTime = ts.toInstant().atOffset(ZoneOffset.UTC);
    }

    public BigDecimal getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(BigDecimal overdueFine) {
        this.overdueFine = overdueFine;
    }
}





