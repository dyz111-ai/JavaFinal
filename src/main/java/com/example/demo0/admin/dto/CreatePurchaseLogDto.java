package com.example.demo0.admin.dto;

import jakarta.validation.constraints.NotNull;

public class CreatePurchaseLogDto {

    @NotNull(message = "管理员ID不能为空")
    private Integer adminId;
    
    private String logText;

    // Getters and Setters
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
    
    public String getLogText() { return logText; }
    public void setLogText(String logText) { this.logText = logText; }
}
