package com.example.demo0.admin.dto;

public class BookRankingDto {

    private String isbn;
    private String title;
    private String author;
    private int metricValue;

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getMetricValue() { return metricValue; }
    public void setMetricValue(int metricValue) { this.metricValue = metricValue; }
}
