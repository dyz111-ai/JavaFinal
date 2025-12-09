package com.example.demo0.book.model;

/**
 * 实体书信息模型
 * 对应数据库 Book 表，包含条码、状态、书架位置等信息
 */
public class PhysicalBook {
    private Integer bookId;
    private String barcode;
    private String status; // 正常/下架/借出
    private Integer shelfId;
    private String isbn;
    private String title; // 来自 BookInfo 表
    private Integer buildingId; // 来自 Bookshelf 表
    private String buildingName; // 来自 Building 表
    private String shelfCode; // 来自 Bookshelf 表
    private Integer floor; // 来自 Bookshelf 表
    private String zone; // 来自 Bookshelf 表

    public PhysicalBook() {}

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getShelfId() {
        return shelfId;
    }

    public void setShelfId(Integer shelfId) {
        this.shelfId = shelfId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getShelfCode() {
        return shelfCode;
    }

    public void setShelfCode(String shelfCode) {
        this.shelfCode = shelfCode;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    /**
     * 获取楼宇名称（优先使用buildingName字段，如果没有则根据buildingId判断）
     */
    public String getBuildingNameDisplay() {
        if (buildingName != null && !buildingName.trim().isEmpty()) {
            return buildingName;
        }
        if (buildingId == null) return "无信息";
        // 根据原项目的逻辑：21=总图书馆，22=德文图书馆（作为后备方案）
        if (buildingId == 21) return "总图书馆";
        if (buildingId == 22) return "德文图书馆";
        return "无信息";
    }
}

