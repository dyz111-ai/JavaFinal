package com.example.demo0.admin.dto;

public class BookAdminDto {

    private String isbn;
    private String title;
    private String author;
    private String publisher;     // 新增
    private String publishDate;   // 新增
    private String location;      // 新增：前端需要显示位置
    private Integer totalCopies;
    private Integer physicalCopies;
    private Integer availableCopies;
    private Integer borrowedCopies;
    private Integer takedownCopies;

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getPhysicalCopies() { return physicalCopies; }
    public void setPhysicalCopies(Integer physicalCopies) { this.physicalCopies = physicalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public Integer getBorrowedCopies() { return borrowedCopies; }
    public void setBorrowedCopies(Integer borrowedCopies) { this.borrowedCopies = borrowedCopies; }

    public Integer getTakedownCopies() { return takedownCopies; }
    public void setTakedownCopies(Integer takedownCopies) { this.takedownCopies = takedownCopies; }
}