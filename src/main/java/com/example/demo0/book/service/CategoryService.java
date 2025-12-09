package com.example.demo0.book.service;

import com.example.demo0.book.model.Category;
import com.example.demo0.book.model.CategoryNode;
import com.example.demo0.book.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务层
 * 对应原项目的 BookCategoryTreeOperation 功能
 */
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService() {
        this.repository = new CategoryRepository();
    }

    /**
     * 获取所有分类
     */
    public List<Category> findAll() {
        return repository.findAll();
    }

    /**
     * 根据ID获取分类
     */
    public Category findById(String categoryId) {
        return repository.findById(categoryId);
    }

    /**
     * 获取分类树
     */
    public List<CategoryNode> getCategoryTree() {
        List<Category> categories = repository.findAll();
        
        // 构建分类字典
        Map<String, CategoryNode> nodeMap = new HashMap<>();
        for (Category c : categories) {
            CategoryNode node = new CategoryNode();
            node.setCategoryId(c.getCategoryId());
            node.setCategoryName(c.getCategoryName());
            node.setParentCategoryId(c.getParentCategoryId());
            node.setChildren(new ArrayList<>());
            nodeMap.put(c.getCategoryId(), node);
        }
        
        // 构建树结构
        List<CategoryNode> rootNodes = new ArrayList<>();
        for (CategoryNode node : nodeMap.values()) {
            String parentId = node.getParentCategoryId();
            if (parentId == null || parentId.isBlank()) {
                // 根节点
                rootNodes.add(node);
            } else if (nodeMap.containsKey(parentId)) {
                // 有父节点，添加到父节点的children
                nodeMap.get(parentId).getChildren().add(node);
            }
        }
        
        return rootNodes;
    }

    /**
     * 添加分类
     */
    public int add(Category category) {
        // 验证分类ID不能为空
        if (category.getCategoryId() == null || category.getCategoryId().isBlank()) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        // 验证分类名称不能为空
        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        // 检查分类ID是否已存在
        if (repository.findById(category.getCategoryId()) != null) {
            throw new IllegalArgumentException("分类ID已存在");
        }
        
        // 检查分类名称在同级中是否重复
        if (repository.isNameDuplicate(category.getCategoryName(), category.getParentCategoryId(), null)) {
            throw new IllegalArgumentException("同级分类中已存在相同名称");
        }
        
        // 如果有父分类，验证父分类是否存在
        if (category.getParentCategoryId() != null && !category.getParentCategoryId().isBlank()) {
            Category parent = repository.findById(category.getParentCategoryId());
            if (parent == null) {
                throw new IllegalArgumentException("父分类不存在");
            }
        }
        
        return repository.add(category);
    }

    /**
     * 更新分类
     */
    public int update(Category category) {
        // 验证分类ID不能为空
        if (category.getCategoryId() == null || category.getCategoryId().isBlank()) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        
        // 验证分类名称不能为空
        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        
        // 检查分类是否存在
        Category existing = repository.findById(category.getCategoryId());
        if (existing == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        // 检查分类名称在同级中是否重复（排除自己）
        if (repository.isNameDuplicate(category.getCategoryName(), category.getParentCategoryId(), category.getCategoryId())) {
            throw new IllegalArgumentException("同级分类中已存在相同名称");
        }
        
        // 如果有父分类，验证父分类是否存在且不能是自己
        if (category.getParentCategoryId() != null && !category.getParentCategoryId().isBlank()) {
            if (category.getParentCategoryId().equals(category.getCategoryId())) {
                throw new IllegalArgumentException("分类不能将自己设为父分类");
            }
            Category parent = repository.findById(category.getParentCategoryId());
            if (parent == null) {
                throw new IllegalArgumentException("父分类不存在");
            }
        }
        
        return repository.update(category);
    }

    /**
     * 删除分类
     */
    public int delete(String categoryId, String operatorId) {
        // 检查分类是否存在
        Category category = repository.findById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        // 检查分类下是否有关联图书
        if (repository.hasBooks(categoryId)) {
            throw new IllegalArgumentException("该分类下还有关联图书，无法删除");
        }
        
        // 检查分类下是否有子分类
        int childCount = repository.getChildCount(categoryId);
        if (childCount > 0) {
            throw new IllegalArgumentException("该分类下还有子分类，无法删除");
        }
        
        return repository.delete(categoryId);
    }

    /**
     * 获取分类路径
     */
    public List<String> getCategoryPath(String categoryId) {
        return repository.getCategoryPath(categoryId);
    }
}

