package com.example.demo0.admin.repository;

import com.example.demo0.admin.dto.CategoryDto;
import jakarta.enterprise.context.ApplicationScoped;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CategoryRepository {

    private DataSource dataSource;

    public CategoryRepository() {
        System.out.println("[CategoryRepository] ========== 开始初始化数据源 ==========");
        try {
            System.out.println("[CategoryRepository] 查找 JNDI 数据源: java:/jdbc/LibraryDS");
            this.dataSource = (DataSource) new InitialContext().lookup("java:/jdbc/LibraryDS");
            System.out.println("[CategoryRepository] ✅ 数据源获取成功");
        } catch (NamingException e) {
            System.err.println("[CategoryRepository] ❌ JNDI 数据源查找失败: " + e.getMessage());
            System.err.println("[CategoryRepository] 异常类型: " + e.getClass().getName());
            e.printStackTrace();
            throw new RuntimeException("JNDI 数据源查找失败", e);
        }
        System.out.println("[CategoryRepository] ========== 数据源初始化完成 ==========");
    }

    /**
     * 获取所有分类（扁平列表）
     */
    public List<CategoryDto> getAllCategories() {
        System.out.println("[CategoryRepository] ========== 开始获取所有分类 ==========");
        
        if (dataSource == null) {
            System.err.println("[CategoryRepository] ❌ 数据源为 null，无法执行查询");
            throw new RuntimeException("数据源未初始化，请检查 JNDI 配置");
        }
        
        List<CategoryDto> list = new ArrayList<>();
        String sql = "SELECT categoryid, categoryname, parentcategoryid FROM public.category ORDER BY categoryid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CategoryDto dto = new CategoryDto();
                dto.setCategoryId(rs.getString("categoryid"));
                dto.setCategoryName(rs.getString("categoryname"));
                dto.setParentCategoryId(rs.getString("parentcategoryid"));
                list.add(dto);
            }
            System.out.println("[CategoryRepository] ✅ 查询完成，共找到 " + list.size() + " 条分类");
            if (list.isEmpty()) {
                System.out.println("[CategoryRepository] ⚠️ 数据库中暂无分类记录");
            } else {
                // 打印前几个分类便于排查
                int preview = Math.min(3, list.size());
                for (int i = 0; i < preview; i++) {
                    CategoryDto d = list.get(i);
                    System.out.println("  -> 分类预览 " + (i + 1) + ": id=" + d.getCategoryId() +
                            ", name=" + d.getCategoryName() +
                            ", parent=" + d.getParentCategoryId());
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 构建分类树
     */
    public List<CategoryDto> getCategoryTree() {
        System.out.println("[CategoryRepository] ========== 开始构建分类树 ==========");
        List<CategoryDto> flatList = getAllCategories();
        
        // 创建映射
        Map<String, CategoryDto> categoryMap = new HashMap<>();
        List<CategoryDto> rootNodes = new ArrayList<>();

        // 第一遍：创建所有节点
        for (CategoryDto dto : flatList) {
            categoryMap.put(dto.getCategoryId(), dto);
        }

        // 第二遍：构建树结构
        for (CategoryDto dto : flatList) {
            if (dto.getParentCategoryId() == null || dto.getParentCategoryId().trim().isEmpty()) {
                // 根节点
                rootNodes.add(dto);
            } else {
                // 子节点
                CategoryDto parent = categoryMap.get(dto.getParentCategoryId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    // 父节点不存在，也作为根节点
                    rootNodes.add(dto);
                }
            }
        }

        System.out.println("[CategoryRepository] ✅ 分类树构建完成，根节点数量: " + rootNodes.size());
        return rootNodes;
    }

    /**
     * 根据ID获取分类
     */
    public CategoryDto getCategoryById(String categoryId) {
        System.out.println("[CategoryRepository] ========== 开始获取分类 ==========");
        System.out.println("[CategoryRepository] CategoryID: " + categoryId);
        
        String sql = "SELECT categoryid, categoryname, parentcategoryid FROM public.category WHERE categoryid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CategoryDto dto = new CategoryDto();
                    dto.setCategoryId(rs.getString("categoryid"));
                    dto.setCategoryName(rs.getString("categoryname"));
                    dto.setParentCategoryId(rs.getString("parentcategoryid"));
                    System.out.println("[CategoryRepository] ✅ 找到分类: " + dto.getCategoryName());
                    return dto;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加分类
     */
    public boolean addCategory(String categoryId, String categoryName, String parentCategoryId) {
        System.out.println("[CategoryRepository] ========== 开始添加分类 ==========");
        System.out.println("[CategoryRepository] CategoryID: " + categoryId);
        System.out.println("[CategoryRepository] CategoryName: " + categoryName);
        System.out.println("[CategoryRepository] ParentCategoryID: " + parentCategoryId);
        
        String sql = "INSERT INTO public.category (categoryid, categoryname, parentcategoryid) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId);
            ps.setString(2, categoryName);
            if (parentCategoryId == null || parentCategoryId.trim().isEmpty()) {
                ps.setString(3, null);
            } else {
                ps.setString(3, parentCategoryId);
            }
            
            int affected = ps.executeUpdate();
            System.out.println("[CategoryRepository] ✅ 添加分类成功，影响行数: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新分类
     */
    public boolean updateCategory(String categoryId, String categoryName, String parentCategoryId) {
        System.out.println("[CategoryRepository] ========== 开始更新分类 ==========");
        System.out.println("[CategoryRepository] CategoryID: " + categoryId);
        System.out.println("[CategoryRepository] CategoryName: " + categoryName);
        System.out.println("[CategoryRepository] ParentCategoryID: " + parentCategoryId);
        
        String sql = "UPDATE public.category SET categoryname = ?, parentcategoryid = ? WHERE categoryid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryName);
            if (parentCategoryId == null || parentCategoryId.trim().isEmpty()) {
                ps.setString(2, null);
            } else {
                ps.setString(2, parentCategoryId);
            }
            ps.setString(3, categoryId);
            
            int affected = ps.executeUpdate();
            System.out.println("[CategoryRepository] ✅ 更新分类成功，影响行数: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除分类
     */
    public boolean deleteCategory(String categoryId) {
        System.out.println("[CategoryRepository] ========== 开始删除分类 ==========");
        System.out.println("[CategoryRepository] CategoryID: " + categoryId);
        
        String sql = "DELETE FROM public.category WHERE categoryid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId);
            
            int affected = ps.executeUpdate();
            System.out.println("[CategoryRepository] ✅ 删除分类成功，影响行数: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查分类下是否有关联图书
     */
    public boolean hasBooksInCategory(String categoryId) {
        System.out.println("[CategoryRepository] ========== 检查分类是否有关联图书 ==========");
        System.out.println("[CategoryRepository] CategoryID: " + categoryId);
        
        String sql = "SELECT COUNT(*) FROM public.book_classify WHERE categoryid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("[CategoryRepository] 关联图书数量: " + count);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取分类的子分类数量
     */
    public int getChildCategoryCount(String categoryId) {
        System.out.println("[CategoryRepository] ========== 获取子分类数量 ==========");
        System.out.println("[CategoryRepository] CategoryID: " + categoryId);
        
        String sql = "SELECT COUNT(*) FROM public.category WHERE parentcategoryid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("[CategoryRepository] 子分类数量: " + count);
                    return count;
                }
            }
        } catch (SQLException e) {
            System.err.println("[CategoryRepository] ❌ SQL异常: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}

