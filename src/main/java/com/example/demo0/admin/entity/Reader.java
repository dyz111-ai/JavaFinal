package com.example.demo0.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reader")
public class Reader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "readerid")
    private Integer readerId;

    @Column(name = "username", nullable = false, length = 8)
    private String username;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @Column(name = "fullname", length = 40)
    private String fullname;

    @Column(name = "nickname", length = 40)
    private String nickname;

    @Column(name = "avatar", length = 200)
    private String avatar;

    @Column(name = "creditscore")
    private Integer creditScore;

    @Column(name = "accountstatus", length = 10)
    private String accountStatus;

    @Column(name = "permission", length = 10)
    private String permission;

    // === Constructors ===
    public Reader() {
    }

    public Reader(String username, String password, String fullname, String nickname,
                  String avatar, Integer creditScore, String accountStatus, String permission) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.nickname = nickname;
        this.avatar = avatar;
        this.creditScore = creditScore;
        this.accountStatus = accountStatus;
        this.permission = permission;
    }

    // === Getters & Setters ===

    public Integer getReaderId() {
        return readerId;
    }

    public void setReaderId(Integer readerId) {
        this.readerId = readerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    // === toString ===
    @Override
    public String toString() {
        return "Reader{" +
                "readerId=" + readerId +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", fullname='" + fullname + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", creditScore=" + creditScore +
                ", accountStatus='" + accountStatus + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }
}
