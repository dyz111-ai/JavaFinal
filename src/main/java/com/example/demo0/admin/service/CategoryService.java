package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.CategoryDto;
import com.example.demo0.admin.repository.CategoryRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class CategoryService {

    private CategoryRepository repository;

    public CategoryService() {
        System.out.println("[CategoryService] ========== Service构造函数 ==========");
        try {
            repository = new CategoryRepository();
            System.out.println("[CategoryService] ✅ CategoryRepository 创建成功");
        } catch (Exception e) {
            System.err.println("[CategoryService] ❌ CategoryRepository 创建失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[CategoryService] ========== Service构造函数完成 ==========");
    }

    /**
     * 获取分类树
     */
    public List<CategoryDto> getCategoryTree() {
        System.out.println("[CategoryService] ========== 获取分类树 ==========");
        if (repository == null) {
            System.err.println("[CategoryService] ❌ Repository 为 null");
            return new java.util.ArrayList<>();
        }
        List<CategoryDto> tree = repository.getCategoryTree();
        int size = tree != null ? tree.size() : 0;
        System.out.println("[CategoryService] ✅ 获取分类树成功，根节点数量: " + size);
        if (size > 0) {
            int preview = Math.min(3, size);
            for (int i = 0; i < preview; i++) {
                CategoryDto d = tree.get(i);
                System.out.println("  -> 根节点预览 " + (i + 1) + ": id=" + d.getCategoryId() + ", name=" + d.getCategoryName());
            }
        } else {
            System.out.println("[CategoryService] ⚠️ 分类树为空");
        }
        return tree != null ? tree : new java.util.ArrayList<>();
    }

    /**
     * 根据ID获取分类
     */
    public CategoryDto getCategoryById(String categoryId) {
        System.out.println("[CategoryService] ========== 获取分类 ==========");
        System.out.println("[CategoryService] CategoryID: " + categoryId);
        if (repository == null) {
            System.err.println("[CategoryService] ❌ Repository 为 null");
            return null;
        }
        return repository.getCategoryById(categoryId);
    }

    /**
     * 添加分类
     */
    public boolean addCategory(String categoryId, String categoryName, String parentCategoryId) {
        System.out.println("[CategoryService] ========== 添加分类 ==========");
        System.out.println("[CategoryService] CategoryID: " + categoryId);
        System.out.println("[CategoryService] CategoryName: " + categoryName);
        System.out.println("[CategoryService] ParentCategoryID: " + parentCategoryId);
        
        if (repository == null) {
            System.err.println("[CategoryService] ❌ Repository 为 null");
            return false;
        }
        
        // 验证分类ID不能为空
        if (categoryId == null || categoryId.trim().isEmpty()) {
            System.err.println("[CategoryService] ❌ 分类ID不能为空");
            return false;
        }
        
        // 验证分类名称不能为空
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.err.println("[CategoryService] ❌ 分类名称不能为空");
            return false;
        }
        
        // 检查分类ID是否已存在
        CategoryDto existing = repository.getCategoryById(categoryId);
        if (existing != null) {
            System.err.println("[CategoryService] ❌ 分类ID已存在: " + categoryId);
            return false;
        }
        
        // 如果指定了父分类，检查父分类是否存在
        if (parentCategoryId != null && !parentCategoryId.trim().isEmpty()) {
            CategoryDto parent = repository.getCategoryById(parentCategoryId);
            if (parent == null) {
                System.err.println("[CategoryService] ❌ 父分类不存在: " + parentCategoryId);
                return false;
            }
        }
        
        return repository.addCategory(categoryId, categoryName, parentCategoryId);
    }

    /**
     * 更新分类
     */
    public boolean updateCategory(String categoryId, String categoryName, String parentCategoryId) {
        System.out.println("[CategoryService] ========== 更新分类 ==========");
        System.out.println("[CategoryService] CategoryID: " + categoryId);
        System.out.println("[CategoryService] CategoryName: " + categoryName);
        System.out.println("[CategoryService] ParentCategoryID: " + parentCategoryId);
        
        if (repository == null) {
            System.err.println("[CategoryService] ❌ Repository 为 null");
            return false;
        }
        
        // 验证分类ID不能为空
        if (categoryId == null || categoryId.trim().isEmpty()) {
            System.err.println("[CategoryService] ❌ 分类ID不能为空");
            return false;
        }
        
        // 验证分类名称不能为空
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.err.println("[CategoryService] ❌ 分类名称不能为空");
            return false;
        }
        
        // 检查分类是否存在
        CategoryDto existing = repository.getCategoryById(categoryId);
        if (existing == null) {
            System.err.println("[CategoryService] ❌ 分类不存在: " + categoryId);
            return false;
        }
        
        // 如果指定了父分类，检查父分类是否存在且不能是自己
        if (parentCategoryId != null && !parentCategoryId.trim().isEmpty()) {
            if (parentCategoryId.equals(categoryId)) {
                System.err.println("[CategoryService] ❌ 不能将自己设为父分类");
                return false;
            }
            CategoryDto parent = repository.getCategoryById(parentCategoryId);
            if (parent == null) {
                System.err.println("[CategoryService] ❌ 父分类不存在: " + parentCategoryId);
                return false;
            }
        }
        
        return repository.updateCategory(categoryId, categoryName, parentCategoryId);
    }

    /**
     * 删除分类
     */
    public boolean deleteCategory(String categoryId) {
        System.out.println("[CategoryService] ========== 删除分类 ==========");
        System.out.println("[CategoryService] CategoryID: " + categoryId);
        
        if (repository == null) {
            System.err.println("[CategoryService] ❌ Repository 为 null");
            return false;
        }
        
        // 检查分类是否存在
        CategoryDto existing = repository.getCategoryById(categoryId);
        if (existing == null) {
            System.err.println("[CategoryService] ❌ 分类不存在: " + categoryId);
            return false;
        }
        
        // 检查是否有子分类
        int childCount = repository.getChildCategoryCount(categoryId);
        if (childCount > 0) {
            System.err.println("[CategoryService] ❌ 该分类下有 " + childCount + " 个子分类，无法删除");
            return false;
        }
        
        // 检查是否有关联图书
        boolean hasBooks = repository.hasBooksInCategory(categoryId);
        if (hasBooks) {
            System.err.println("[CategoryService] ❌ 该分类下有关联图书，无法删除");
            return false;
        }
        
        return repository.deleteCategory(categoryId);
    }
}

