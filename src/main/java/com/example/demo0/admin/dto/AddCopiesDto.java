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
    private Integer buildingId;

    @NotNull
    private Integer floor;

    @NotBlank
    private String zone;

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getNumberOfCopies() { return numberOfCopies; }
    public void setNumberOfCopies(Integer numberOfCopies) { this.numberOfCopies = numberOfCopies; }

    public Integer getBuildingId() { return buildingId; }
    public void setBuildingId(Integer buildingId) { this.buildingId = buildingId; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
}
