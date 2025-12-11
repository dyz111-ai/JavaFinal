package com.example.demo0.admin.dto;

import java.time.LocalDateTime;

public class PurchaseLogDto {

    private Long logId;
    private String logText;
    private LocalDateTime logDate;
    private Integer adminId;

    // Getters and Setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public String getLogText() { return logText; }
    public void setLogText(String logText) { this.logText = logText; }

    public LocalDateTime getLogDate() { return logDate; }
    public void setLogDate(LocalDateTime logDate) { this.logDate = logDate; }
    
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
}
