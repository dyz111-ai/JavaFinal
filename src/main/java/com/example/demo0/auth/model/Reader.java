package com.example.demo0.auth.model;

public class Reader {
    private Integer readerId;
    private String username;
    private String password;
    private String fullname;
    private String nickname;
    private String avatar;
    private Integer creditScore;
    private String accountStatus; // "正常" 或 "冻结"
    private String permission;    // "普通" 或 "高级"

    // Getters and Setters
    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}