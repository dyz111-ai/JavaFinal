package com.example.demo0.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comment_Table")
public class Comment_Table {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentid")
    private Integer commentId;

    @Column(name = "readerid", nullable = false)
    private Integer readerId;

    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;

    @Column(name = "rating")
    private Integer rating;  // 1-5

    @Lob
    @Column(name = "reviewcontent")
    private String reviewContent;

    @Column(name = "createtime")
    private LocalDateTime createTime;

    @Column(name = "status", length = 10)
    private String status; // 正常 / 已删除

    // === Constructors ===
    public Comment_Table() {
    }

    public Comment_Table(Integer readerId, String isbn, Integer rating,
                         String reviewContent, LocalDateTime createTime,
                         String status) {
        this.readerId = readerId;
        this.isbn = isbn;
        this.rating = rating;
        this.reviewContent = reviewContent;
        this.createTime = createTime;
        this.status = status;
    }

    // === Getters & Setters ===
    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getReaderId() {
        return readerId;
    }

    public void setReaderId(Integer readerId) {
        this.readerId = readerId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // === toString() ===
    @Override
    public String toString() {
        return "Comment_Table{" +
                "commentId=" + commentId +
                ", readerId=" + readerId +
                ", isbn='" + isbn + '\'' +
                ", rating=" + rating +
                ", reviewContent='" + reviewContent + '\'' +
                ", createTime=" + createTime +
                ", status='" + status + '\'' +
                '}';
    }
}
