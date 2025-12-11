package com.example.demo0.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logid")
    private long logId;

    @Column(name = "operationtime")
    private LocalDateTime operationTime;

    @Lob
    @Column(name = "operationcontent", nullable = false)
    private String operationContent;

    @Column(name = "operatortype", nullable = false, length = 10)
    private String operatorType; // Reader / Librarian

    @Column(name = "operatorid")
    private Integer operatorId;

    @Column(name = "operationstatus", nullable = false, length = 10)
    private String operationStatus; // 成功 / 失败

    @Lob
    @Column(name = "errormessage")
    private String errorMessage;

    // === Constructors ===
    public Log() {
    }

    public Log(LocalDateTime operationTime, String operationContent, String operatorType,
               Integer operatorId, String operationStatus, String errorMessage) {
        this.operationTime = operationTime;
        this.operationContent = operationContent;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.operationStatus = operationStatus;
        this.errorMessage = errorMessage;
    }

    // === Getters & Setters ===

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    public String getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(String operationContent) {
        this.operationContent = operationContent;
    }

    public String getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(String operatorType) {
        this.operatorType = operatorType;
    }

    public Integer getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // === toString ===
    @Override
    public String toString() {
        return "Log{" +
                "logId=" + logId +
                ", operationTime=" + operationTime +
                ", operationContent='" + operationContent + '\'' +
                ", operatorType='" + operatorType + '\'' +
                ", operatorId=" + operatorId +
                ", operationStatus='" + operationStatus + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
