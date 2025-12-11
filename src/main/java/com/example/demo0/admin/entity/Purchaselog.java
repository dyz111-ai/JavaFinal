package com.example.demo0.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchaselog")
public class Purchaselog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logid")
    private Long logId;

    @Column(name = "logtext", nullable = false, length = 1000)
    private String logText;

    @Column(name = "adminid")
    private Integer adminId;

    @Column(name = "logdate")
    private LocalDateTime logDate;


    public Purchaselog() {}

    public Purchaselog(String logText, Integer adminId, LocalDateTime logDate) {
        this.logText = logText;
        this.adminId = adminId;
        this.logDate = logDate;
    }

    // ======================
    // Getters & Setters
    // ======================

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public LocalDateTime getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }

    @Override
    public String toString() {
        return "Purchaselog{" +
                "logId=" + logId +
                ", logText='" + logText + '\'' +
                ", adminId=" + adminId +
                ", logDate=" + logDate +
                '}';
    }
}
