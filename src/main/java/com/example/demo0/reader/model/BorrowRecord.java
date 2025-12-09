package com.example.demo0.reader.model;

import java.time.OffsetDateTime;
import java.sql.Timestamp;
import java.time.ZoneOffset;

/**
 * 借阅记录模型
 */
public class BorrowRecord {
    private Integer borrowRecordId;
    private Integer readerId;
    private Integer bookId;
    private OffsetDateTime borrowTime;
    private OffsetDateTime returnTime;
    private java.math.BigDecimal overdueFine;

    // Getters and Setters
    public Integer getBorrowRecordId() {
        return borrowRecordId;
    }

    public void setBorrowRecordId(Integer borrowRecordId) {
        this.borrowRecordId = borrowRecordId;
    }

    public Integer getReaderId() {
        return readerId;
    }

    public void setReaderId(Integer readerId) {
        this.readerId = readerId;
    }

    public void setReaderId(String readerId) {
        try {
            this.readerId = Integer.parseInt(readerId);
        } catch (NumberFormatException e) {
            this.readerId = null;
        }
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public void setBookId(String bookId) {
        try {
            this.bookId = Integer.parseInt(bookId);
        } catch (NumberFormatException e) {
            this.bookId = null;
        }
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

    public java.math.BigDecimal getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(java.math.BigDecimal overdueFine) {
        this.overdueFine = overdueFine;
    }
}

