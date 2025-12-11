package com.example.demo0.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理员登录DTO
 */
public class AdminLoginDto {
    
    @NotBlank(message = "工号不能为空")
    private String staffNo;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    // Getters and Setters
    public String getStaffNo() {
        return staffNo;
    }
    
    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}