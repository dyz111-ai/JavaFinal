package com.example.demo0.admin.dto;

/**
 * 分类请求DTO（用于创建和更新分类）
 */
public class CategoryRequest {
    private String categoryId;
    private String categoryName;
    private String parentCategoryId;

    public CategoryRequest() {}

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

