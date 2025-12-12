package com.example.demo0.admin.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类DTO（用于管理员分类管理）
 */
public class CategoryDto {
    private String categoryId;
    private String categoryName;
    private String parentCategoryId;
    private List<CategoryDto> children = new ArrayList<>();

    public CategoryDto() {}

    public CategoryDto(String categoryId, String categoryName, String parentCategoryId) {
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

    public List<CategoryDto> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryDto> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}

