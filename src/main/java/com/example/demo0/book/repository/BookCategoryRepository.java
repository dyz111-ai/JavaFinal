package com.example.demo0.book.repository;

import com.example.demo0.book.model.BookCategory;
import com.example.demo0.book.model.BookCategoryDetail;

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
 * 图书分类关联数据访问层
 * 对应原项目的 BookCategoryRepository
 */
public class BookCategoryRepository {

    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";
    private final DataSource dataSource;
    private final CategoryRepository categoryRepository;

    public BookCategoryRepository() {
        this.dataSource = lookupDataSource();
        this.categoryRepository = new CategoryRepository();
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
     * 添加图书分类关联
     */
    public int add(BookCategory bookCategory) {
        if (bookCategory == null || bookCategory.getIsbn() == null || bookCategory.getCategoryId() == null) {
            throw new IllegalArgumentException("ISBN和分类ID不能为空");
        }
        String sql = "INSERT INTO public.book_classify (isbn, categoryid, relationnote) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, bookCategory.getIsbn().trim());
            ps.setString(2, bookCategory.getCategoryId().trim());
            ps.setString(3, bookCategory.getRelationNote());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("添加图书分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量添加图书分类关联
     */
    public int addAll(List<BookCategory> bookCategories) {
        if (bookCategories == null || bookCategories.isEmpty()) {
            return 0;
        }
        String sql = "INSERT INTO public.book_classify (isbn, categoryid, relationnote) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            int count = 0;
            for (BookCategory bc : bookCategories) {
                ps.setString(1, bc.getIsbn().trim());
                ps.setString(2, bc.getCategoryId().trim());
                ps.setString(3, bc.getRelationNote());
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            return count;
        } catch (SQLException e) {
            throw new RuntimeException("批量添加图书分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除图书分类关联
     */
    public int remove(String isbn, String categoryId) {
        if (isbn == null || isbn.isBlank() || categoryId == null || categoryId.isBlank()) {
            throw new IllegalArgumentException("ISBN和分类ID不能为空");
        }
        String sql = "DELETE FROM public.book_classify WHERE isbn = ? AND categoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, isbn.trim());
            ps.setString(2, categoryId.trim());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除图书分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除图书的所有分类关联
     */
    public int removeAllByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("ISBN不能为空");
        }
        String sql = "DELETE FROM public.book_classify WHERE isbn = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, isbn.trim());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除图书所有分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查图书分类关联是否存在
     */
    public boolean exists(String isbn, String categoryId) {
        if (isbn == null || isbn.isBlank() || categoryId == null || categoryId.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM public.book_classify WHERE isbn = ? AND categoryid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, isbn.trim());
            ps.setString(2, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("检查图书分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取图书的所有分类关联
     */
    public List<BookCategoryDetail> findByIsbn(String isbn) {
        System.out.println("[BookCategoryRepository] ========== 开始查询图书分类 ==========");
        System.out.println("[BookCategoryRepository] ISBN: " + isbn);
        
        if (isbn == null || isbn.isBlank()) {
            System.out.println("[BookCategoryRepository] ❌ ISBN为空，返回空列表");
            return new ArrayList<>();
        }

        // 调试：查看 book_classify 表现有的部分记录，确认列名/数据
        debugPrintSampleRows();
        
        String sql = "SELECT " +
                     "    bc.isbn, " +
                     "    bi.title, " +
                     "    bi.author, " +
                     "    bc.categoryid, " +
                     "    c.categoryname, " +
                     "    bc.relationnote " +
                     "FROM public.book_classify bc " +
                     "JOIN public.bookinfo bi ON bc.isbn = bi.isbn " +
                     "JOIN public.category c ON bc.categoryid = c.categoryid " +
                     "WHERE bc.isbn = ? " +
                     "ORDER BY c.categoryname";
        
        System.out.println("[BookCategoryRepository] SQL: " + sql);
        System.out.println("[BookCategoryRepository] 参数: isbn=" + isbn.trim());
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                List<BookCategoryDetail> list = new ArrayList<>();
                int count = 0;
                while (rs.next()) {
                    count++;
                    BookCategoryDetail detail = new BookCategoryDetail();
                    detail.setIsbn(nullSafe(rs.getString("isbn")));
                    detail.setTitle(nullSafe(rs.getString("title")));
                    detail.setAuthor(nullSafe(rs.getString("author")));
                    detail.setCategoryId(nullSafe(rs.getString("categoryid")));
                    detail.setCategoryName(nullSafe(rs.getString("categoryname")));
                    detail.setRelationNote(nullSafe(rs.getString("relationnote")));
                    
                    System.out.println("[BookCategoryRepository] 找到分类 " + count + ": " +
                                     "categoryId=" + detail.getCategoryId() + 
                                     ", categoryName=" + detail.getCategoryName());
                    
                    // 获取分类路径
                    String categoryPath = String.join(" / ", categoryRepository.getCategoryPath(detail.getCategoryId()));
                    detail.setCategoryPath(categoryPath);
                    
                    System.out.println("[BookCategoryRepository] 分类路径: " + categoryPath);
                    
                    list.add(detail);
                }
                System.out.println("[BookCategoryRepository] ✅ 查询完成，共找到 " + count + " 条分类记录");
                return list;
            }
        } catch (SQLException e) {
            System.err.println("[BookCategoryRepository] ❌ SQL异常: " + e.getMessage());
            System.err.println("[BookCategoryRepository] SQL状态: " + e.getSQLState());
            e.printStackTrace();
            throw new RuntimeException("查询图书分类关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调试：打印 book_classify 表的前几条记录，便于核对列名与数据
     */
    private void debugPrintSampleRows() {
        String sampleSql = "SELECT isbn, categoryid FROM public.book_classify LIMIT 5";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sampleSql);
             ResultSet rs = ps.executeQuery()) {
            int c = 0;
            while (rs.next()) {
                c++;
                System.out.println("[BookCategoryRepository][DEBUG] sample " + c +
                        " isbn=" + rs.getString("isbn") +
                        ", categoryid=" + rs.getString("categoryid"));
            }
            if (c == 0) {
                System.out.println("[BookCategoryRepository][DEBUG] book_classify 表暂无记录");
            }
        } catch (SQLException e) {
            System.err.println("[BookCategoryRepository][DEBUG] 读取样例失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类的所有图书关联
     */
    public List<BookCategoryDetail> findByCategoryId(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return new ArrayList<>();
        }
        String sql = "SELECT " +
                     "    bc.isbn, " +
                     "    bi.title, " +
                     "    bi.author, " +
                     "    bc.categoryid, " +
                     "    c.categoryname, " +
                     "    bc.relationnote " +
                     "FROM public.book_classify bc " +
                     "JOIN public.bookinfo bi ON bc.isbn = bi.isbn " +
                     "JOIN public.category c ON bc.categoryid = c.categoryid " +
                     "WHERE bc.categoryid = ? " +
                     "ORDER BY bi.title";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, categoryId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                List<BookCategoryDetail> list = new ArrayList<>();
                while (rs.next()) {
                    BookCategoryDetail detail = new BookCategoryDetail();
                    detail.setIsbn(nullSafe(rs.getString("isbn")));
                    detail.setTitle(nullSafe(rs.getString("title")));
                    detail.setAuthor(nullSafe(rs.getString("author")));
                    detail.setCategoryId(nullSafe(rs.getString("categoryid")));
                    detail.setCategoryName(nullSafe(rs.getString("categoryname")));
                    detail.setRelationNote(nullSafe(rs.getString("relationnote")));
                    
                    // 获取分类路径
                    String categoryPath = String.join(" / ", categoryRepository.getCategoryPath(detail.getCategoryId()));
                    detail.setCategoryPath(categoryPath);
                    
                    list.add(detail);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询分类图书关联失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查是否为叶子节点分类（没有子分类的分类）
     */
    public boolean isLeafCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return false;
        }
        return categoryRepository.getChildCount(categoryId) == 0;
    }

    /**
     * 获取所有叶子节点分类
     */
    public List<com.example.demo0.book.model.Category> findLeafCategories() {
        String sql = "SELECT c.categoryid, c.categoryname, c.parentcategoryid " +
                     "FROM public.category c " +
                     "WHERE NOT EXISTS (" +
                     "    SELECT 1 FROM public.category child WHERE child.parentcategoryid = c.categoryid" +
                     ") " +
                     "ORDER BY c.categoryname";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            List<com.example.demo0.book.model.Category> list = new ArrayList<>();
            while (rs.next()) {
                com.example.demo0.book.model.Category c = new com.example.demo0.book.model.Category();
                c.setCategoryId(nullSafe(rs.getString("categoryid")));
                c.setCategoryName(nullSafe(rs.getString("categoryname")));
                c.setParentCategoryId(nullSafe(rs.getString("parentcategoryid")));
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询叶子分类失败: " + e.getMessage(), e);
        }
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}

