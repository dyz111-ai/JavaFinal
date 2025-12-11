package com.example.demo0.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reportid")
    private Integer reportId;

    @Column(name = "commentid", nullable = false)
    private Integer commentId;

    @Column(name = "readerid", nullable = false)
    private Integer readerId;

    @Lob
    @Column(name = "reportreason")
    private String reportReason;

    @Column(name = "reporttime")
    private LocalDateTime reportTime;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // 待处理 / 驳回 / 处理完成

    @Column(name = "librarianid")
    private Integer librarianId;

    // === Constructors ===
    public Report() {
    }

    public Report(Integer commentId, Integer readerId, String reportReason,
                  LocalDateTime reportTime, String status, Integer librarianId) {
        this.commentId = commentId;
        this.readerId = readerId;
        this.reportReason = reportReason;
        this.reportTime = reportTime;
        this.status = status;
        this.librarianId = librarianId;
    }

    // === Getters & Setters ===

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(Integer librarianId) {
        this.librarianId = librarianId;
    }

    // === toString ===
    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", commentId=" + commentId +
                ", readerId=" + readerId +
                ", reportReason='" + reportReason + '\'' +
                ", reportTime=" + reportTime +
                ", status='" + status + '\'' +
                ", librarianId=" + librarianId +
                '}';
    }
}
