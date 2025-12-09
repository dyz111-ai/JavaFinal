package com.example.demo0.book.model;

/**
 * 图书分类模型
 * 对应数据库 Category 表
 */
public class Category {
    private String categoryId;
    private String categoryName;
    private String parentCategoryId;

    public Category() {}

    public Category(String categoryId, String categoryName, String parentCategoryId) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.parentCategoryId = parentCategoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }
}

