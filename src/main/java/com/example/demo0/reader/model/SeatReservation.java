package com.example.demo0.reader.model;

import java.time.LocalDateTime;

public class SeatReservation {

    private String seatCode;
    private int buildingId;
    private String buildingName;
    private int floor;
    private Integer readerId;
    private String nickname;
    private LocalDateTime reservedAt;
    private LocalDateTime endAt;
    private String status;

    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public int getBuildingId() { return buildingId; }
    public void setBuildingId(int buildingId) { this.buildingId = buildingId; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    public Integer getReaderId() { return readerId; }
    public void setReaderId(Integer readerId) { this.readerId = readerId; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public LocalDateTime getReservedAt() { return reservedAt; }
    public void setReservedAt(LocalDateTime reservedAt) { this.reservedAt = reservedAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

