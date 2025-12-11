package com.example.demo0.admin.dto;

import java.time.LocalDateTime;

public class ReportDetailDto {

    private Integer reportId;
    private String reportReason;
    private LocalDateTime reportTime;
    private String reportStatus;

    private Integer commentId;
    private String reviewContent;
    private LocalDateTime commentTime;

    private Integer commenterId;
    private String commenterNickname;
    private String commenterAccountStatus;

    private Integer reporterId;
    private String reporterNickname;

    private String isbn;
    private String bookTitle;

    // ReportDetailDto.java 中添加
    private Integer librarianId;

    // ---------------- Getters & Setters ----------------

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public String getReportReason() {
        return reportReason;
    }

    public void setReportReason(String reportReason) {
        this.reportReason = reportReason;
    }

    public LocalDateTime getReportTime() {
        return reportTime;
    }

    public void setReportTime(LocalDateTime reportTime) {
        this.reportTime = reportTime;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getReviewContent() {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent) {
        this.reviewContent = reviewContent;
    }

    public LocalDateTime getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(LocalDateTime commentTime) {
        this.commentTime = commentTime;
    }

    public Integer getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(Integer commenterId) {
        this.commenterId = commenterId;
    }

    public String getCommenterNickname() {
        return commenterNickname;
    }

    public void setCommenterNickname(String commenterNickname) {
        this.commenterNickname = commenterNickname;
    }

    public String getCommenterAccountStatus() {
        return commenterAccountStatus;
    }

    public void setCommenterAccountStatus(String commenterAccountStatus) {
        this.commenterAccountStatus = commenterAccountStatus;
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterNickname() {
        return reporterNickname;
    }

    public void setReporterNickname(String reporterNickname) {
        this.reporterNickname = reporterNickname;
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

    public Integer getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(Integer librarianId) {
        this.librarianId = librarianId;
    }
}
