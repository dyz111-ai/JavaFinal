package com.example.demo0.book.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.sql.Timestamp;

public class CommentRecord {
    private Long readerId;
    private String isbn;
    private Short rating;
    private String reviewContent;
    private OffsetDateTime createTime;
    private String status;

    public Long getReaderId() { return readerId; }
    public void setReaderId(Long readerId) { this.readerId = readerId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Short getRating() { return rating; }
    public void setRating(Short rating) { this.rating = rating; }

    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }

    public OffsetDateTime getCreateTime() { return createTime; }
    public void setCreateTime(OffsetDateTime createTime) { this.createTime = createTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setCreateTime(Timestamp ts) {
        if (ts == null) { this.createTime = null; return; }
        this.createTime = ts.toInstant().atOffset(ZoneOffset.UTC);
    }
}
