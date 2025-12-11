package com.example.demo0.admin.dto;

/**
 * 更新图书位置的DTO类
 */
public class UpdateBookLocationDto {
    private String building;
    private String floor;
    private String zone;
    private String shelf;
    private String location; // 完整的位置字符串，格式如：A栋1楼A区1号架

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
