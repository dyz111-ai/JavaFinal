package com.example.demo0.book.repository;

import com.example.demo0.book.model.Category;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类数据访问层
 * 对应原项目的 BookCategoryTreeOperation
 */
public class CategoryRepository {

    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";
    private final DataSource dataSource;

    public CategoryRepository() {
        this.dataSource = lookupDataSource();
    }

    private DataSource lookupDataSource() {
        try {
            InitialContext ctx = new InitialContext();
            return (DataSource) ctx.lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException("无法通过 JNDI 查找到数据源: " + JNDI_NAME, e);
        }
    }

    /**
     * 获取所有分类
     */
    public List<Category> findAll() {
        String sql = "SELECT categoryid, categoryname, parentcategoryid FROM public.category ORDER BY categoryid";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            List<Category> list = new ArrayList<>();
            while (rs.next()) {
                Category c = new Category();
                c.setCategoryId(nullSafe(rs.getString("categoryid")));
                c.setCategoryName(nullSafe(rs.getString("categoryname")));
                // parentcategoryid 为 NULL 时保留为 null，不转换为空字符串
                String parentId = rs.getString("parentcategoryid");
                c.setParentCategoryId(parentId == null ? null : parentId.trim());
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID获取分类
     */
    public Category findById(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return null;
        }
        String sql = "SELECT categoryid, categoryname, parentcategoryid FROM public.category WHERE categoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Category c = new Category();
                    c.setCategoryId(nullSafe(rs.getString("categoryid")));
                    c.setCategoryName(nullSafe(rs.getString("categoryname")));
                    // parentcategoryid 为 NULL 时保留为 null，不转换为空字符串
                    String parentId = rs.getString("parentcategoryid");
                    c.setParentCategoryId(parentId == null ? null : parentId.trim());
                    return c;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("按ID查询分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加分类
     */
    public int add(Category category) {
        if (category == null || category.getCategoryId() == null || category.getCategoryId().isBlank()) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        String sql = "INSERT INTO public.category (categoryid, categoryname, parentcategoryid) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, category.getCategoryId().trim());
            ps.setString(2, category.getCategoryName() != null ? category.getCategoryName().trim() : "");
            ps.setString(3, category.getParentCategoryId() != null && !category.getParentCategoryId().isBlank() 
                ? category.getParentCategoryId().trim() : null);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("添加分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新分类
     */
    public int update(Category category) {
        if (category == null || category.getCategoryId() == null || category.getCategoryId().isBlank()) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        String sql = "UPDATE public.category SET categoryname = ?, parentcategoryid = ? WHERE categoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, category.getCategoryName() != null ? category.getCategoryName().trim() : "");
            ps.setString(2, category.getParentCategoryId() != null && !category.getParentCategoryId().isBlank() 
                ? category.getParentCategoryId().trim() : null);
            ps.setString(3, category.getCategoryId().trim());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除分类
     */
    public int delete(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("分类ID不能为空");
        }
        String sql = "DELETE FROM public.category WHERE categoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId.trim());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除分类失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查分类下是否有关联图书
     */
    public boolean hasBooks(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM public.book_classify WHERE categoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查分类图书关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取子分类数量
     */
    public int getChildCount(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM public.category WHERE parentcategoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("获取子分类数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查分类名称在同级中是否重复
     */
    public boolean isNameDuplicate(String categoryName, String parentCategoryId, String excludeCategoryId) {
        if (categoryName == null || categoryName.isBlank()) {
            return false;
        }
        
        String sql;
        if (excludeCategoryId != null && !excludeCategoryId.isBlank()) {
            if (parentCategoryId != null && !parentCategoryId.isBlank()) {
                sql = "SELECT COUNT(*) FROM public.category WHERE categoryname = ? AND parentcategoryid = ? AND categoryid != ?";
            } else {
                sql = "SELECT COUNT(*) FROM public.category WHERE categoryname = ? AND parentcategoryid IS NULL AND categoryid != ?";
            }
        } else {
            if (parentCategoryId != null && !parentCategoryId.isBlank()) {
                sql = "SELECT COUNT(*) FROM public.category WHERE categoryname = ? AND parentcategoryid = ?";
            } else {
                sql = "SELECT COUNT(*) FROM public.category WHERE categoryname = ? AND parentcategoryid IS NULL";
            }
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            ps.setString(paramIndex++, categoryName.trim());
            if (parentCategoryId != null && !parentCategoryId.isBlank()) {
                ps.setString(paramIndex++, parentCategoryId.trim());
            }
            if (excludeCategoryId != null && !excludeCategoryId.isBlank()) {
                ps.setString(paramIndex, excludeCategoryId.trim());
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查分类名称重复失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取分类路径（从根到当前节点）
     */
    public List<String> getCategoryPath(String categoryId) {
        List<String> path = new ArrayList<>();
        String currentId = categoryId;
        
        while (currentId != null && !currentId.isBlank()) {
            Category category = findById(currentId);
            if (category == null) break;
            
            path.add(0, category.getCategoryName()); // 插入到开头
            currentId = category.getParentCategoryId();
        }
        
        return path;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}

