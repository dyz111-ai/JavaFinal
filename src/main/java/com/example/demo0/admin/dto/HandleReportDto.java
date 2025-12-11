package com.example.demo0.admin.dto;

import jakarta.validation.constraints.*;

public class HandleReportDto {

    @NotNull(message = "举报ID不能为空")
    private Integer reportId;

    @NotNull(message = "管理员ID不能为空")
    private Integer librarianId;

    @NotBlank(message = "新的举报状态不能为空")
    private String newReportStatus;

    private Integer commentId;
    
    private String newCommentStatus;
    
    private Integer commenterId;

    private boolean banUser = false;
    
    // 用于接收前端传入的处理动作（approve/reject）
    @NotBlank(message = "处理动作不能为空")
    private String action;

    // Getters and Setters
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public Integer getLibrarianId() { return librarianId; }
    public void setLibrarianId(Integer librarianId) { this.librarianId = librarianId; }

    public String getNewReportStatus() { return newReportStatus; }
    public void setNewReportStatus(String newReportStatus) { this.newReportStatus = newReportStatus; }

    public Integer getCommentId() { return commentId; }
    public void setCommentId(Integer commentId) { this.commentId = commentId; }

    public String getNewCommentStatus() { return newCommentStatus; }
    public void setNewCommentStatus(String newCommentStatus) { this.newCommentStatus = newCommentStatus; }

    public Integer getCommenterId() { return commenterId; }
    public void setCommenterId(Integer commenterId) { this.commenterId = commenterId; }

    public boolean isBanUser() { return banUser; }
    public void setBanUser(boolean banUser) { this.banUser = banUser; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
