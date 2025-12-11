package com.example.demo0.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateBookDto {

    @NotBlank(message = "图书isbn号不能为空")
    private String isbn;

    @NotBlank(message = "书名不能为空")
    private String title;
    
    @NotBlank(message = "作者不能为空")
    private String author;

    // Getters and Setters
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
