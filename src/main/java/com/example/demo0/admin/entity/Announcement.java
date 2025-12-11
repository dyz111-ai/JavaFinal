package com.example.demo0.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcementId")
    private Integer announcementId;

    @Column(name = "librarianid", nullable = false)
    private Integer librarianId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "createtime")
    private LocalDateTime createTime;

    @Column(name = "targetgroup", nullable = false, length = 10)
    private String targetGroup;

    @Column(name = "status", nullable = false, length = 10)
    private String status; // 发布中 / 已撤回

    // === Constructors ===
    public Announcement() {
    }

    public Announcement(Integer librarianId, String title, String content,
                        LocalDateTime createTime, String targetGroup, String status) {
        this.librarianId = librarianId;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.targetGroup = targetGroup;
        this.status = status;
    }

    // === Getters & Setters ===
    public Integer getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(Integer announcementId) {
        this.announcementId = announcementId;
    }

    public Integer getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(Integer librarianId) {
        this.librarianId = librarianId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
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
        return "Announcement{" +
                "announcementId=" + announcementId +
                ", librarianId=" + librarianId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", targetGroup='" + targetGroup + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
