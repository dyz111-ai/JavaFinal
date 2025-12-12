package com.example.demo0.admin.dto;

public class BookAdminDto {

    private Integer bookId;       // book表的bookid
    private String isbn;
    private String title;
    private String author;
    private String publisher;     // 新增
    private String publishDate;   // 新增
    private String barcode;        // book表的barcode
    private String status;         // book表的status
    private String location;       // 通过shelfid获取的shelfcode
    private Integer totalCopies;
    private Integer physicalCopies;
    private Integer availableCopies;
    private Integer borrowedCopies;
    private Integer takedownCopies;

    // Getters and Setters
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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