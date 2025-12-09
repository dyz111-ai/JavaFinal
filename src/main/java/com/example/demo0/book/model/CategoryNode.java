package com.example.demo0.book.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点模型
 * 用于构建分类树结构
 */
public class CategoryNode {
    private String categoryId;
    private String categoryName;
    private String parentCategoryId;
    private List<CategoryNode> children = new ArrayList<>();

    public CategoryNode() {}

    public CategoryNode(String categoryId, String categoryName, String parentCategoryId) {
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

    public List<CategoryNode> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryNode> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}

