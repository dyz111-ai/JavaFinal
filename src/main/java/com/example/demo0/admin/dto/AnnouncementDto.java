package com.example.demo0.admin.dto;

import java.time.LocalDateTime;

public class AnnouncementDto {

    private Integer announcementId;
    private Integer librarianId;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private String targetGroup;
    private String status;

    // Getters and Setters
    public Integer getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Integer announcementId) { this.announcementId = announcementId; }

    public Integer getLibrarianId() { return librarianId; }
    public void setLibrarianId(Integer librarianId) { this.librarianId = librarianId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getTargetGroup() { return targetGroup; }
    public void setTargetGroup(String targetGroup) { this.targetGroup = targetGroup; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
