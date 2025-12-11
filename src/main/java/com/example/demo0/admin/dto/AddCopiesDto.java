package com.example.demo0.admin.dto;

import jakarta.validation.constraints.*;

public class AddCopiesDto {

    @NotBlank
    private String isbn;

    @NotNull
    @Min(1)
    @Max(100)
    private Integer numberOfCopies;

    @NotNull
    private Integer shelfId;

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getNumberOfCopies() { return numberOfCopies; }
    public void setNumberOfCopies(Integer numberOfCopies) { this.numberOfCopies = numberOfCopies; }

    public Integer getShelfId() { return shelfId; }
    public void setShelfId(Integer shelfId) { this.shelfId = shelfId; }
}
