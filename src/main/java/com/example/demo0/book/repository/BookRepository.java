package com.example.demo0.book.repository;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.model.PhysicalBook;

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
 * 通过 WildFly 的 JNDI 数据源（java:/jdbc/LibraryDS）查询 PostgreSQL 图书数据。
 * 表结构示例：INSERT INTO BookInfo (ISBN, Title, Author, Stock) VALUES (...)
 * 注意：PostgreSQL 未加双引号的标识符会自动转小写，因此代码中使用 public.bookinfo 与列名 isbn/title/author/stock。
 */
public class BookRepository {

    private static final String JNDI_NAME = "java:/jdbc/LibraryDS"; // 与服务器配置一致
    private final DataSource dataSource;

    public BookRepository() {
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
     * 关键词搜索：在 SQL 层用 ILIKE（忽略大小写）匹配 title/author/isbn。
     * 若 keyword 为空，则返回前 200 条。
     * 包含分类信息（使用 LISTAGG 聚合分类名称）
     */
    public List<BookInfo> search(String keyword) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        // 使用 LEFT JOIN 关联分类，并用 STRING_AGG 聚合分类名称（PostgreSQL语法）
        String base = "SELECT " +
                     "    bi.isbn, " +
                     "    bi.title, " +
                     "    bi.author, " +
                     "    bi.stock, " +
                     "    STRING_AGG(DISTINCT c.categoryname, ', ' ORDER BY c.categoryname) AS categories " +
                     "FROM public.bookinfo bi " +
                     "LEFT JOIN public.book_classify bc ON bi.isbn = bc.isbn " +
                     "LEFT JOIN public.category c ON bc.categoryid = c.categoryid";
        String where = hasKeyword ? " WHERE bi.isbn ILIKE ? OR bi.title ILIKE ? OR bi.author ILIKE ?" : "";
        String suffix = " GROUP BY bi.isbn, bi.title, bi.author, bi.stock " +
                        "ORDER BY bi.title NULLS LAST LIMIT 200";
        String sql = base + where + suffix;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<BookInfo> list = new ArrayList<>();
                while (rs.next()) {
                    BookInfo b = new BookInfo();
                    b.setISBN(nullSafe(rs.getString("isbn")));
                    b.setTitle(nullSafe(rs.getString("title")));
                    b.setAuthor(nullSafe(rs.getString("author")));
                    int stock = rs.getInt("stock");
                    if (rs.wasNull()) {
                        b.setStock(null);
                    } else {
                        b.setStock(stock);
                    }
                    // 解析分类信息（逗号分隔的字符串）
                    String categoriesStr = nullSafe(rs.getString("categories"));
                    List<String> categoriesList = new ArrayList<>();
                    if (categoriesStr != null && !categoriesStr.isBlank()) {
                        String[] categories = categoriesStr.split(",\\s*");
                        for (String cat : categories) {
                            if (cat != null && !cat.trim().isBlank()) {
                                categoriesList.add(cat.trim());
                            }
                        }
                    }
                    b.setCategories(categoriesList);
                    list.add(b);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询 bookinfo 失败: " + e.getMessage(), e);
        }
    }

    public BookInfo findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) return null;
        String sql = "SELECT isbn, title, author, stock FROM public.bookinfo WHERE isbn = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BookInfo b = new BookInfo();
                    b.setISBN(nullSafe(rs.getString("isbn")));
                    b.setTitle(nullSafe(rs.getString("title")));
                    b.setAuthor(nullSafe(rs.getString("author")));
                    int stock = rs.getInt("stock");
                    if (rs.wasNull()) {
                        b.setStock(null);
                    } else {
                        b.setStock(stock);
                    }
                    return b;
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("按 ISBN 查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据书名或ISBN查询实体书信息（包括条码、书架位置等）
     * 对应原项目的 SearchBookWhichShelfAsync 方法
     */
    public List<PhysicalBook> findPhysicalBooksByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 根据原项目的SQL，查询Book表关联BookInfo、Bookshelf和Building表
        // 注意：PostgreSQL表名可能是小写，需要根据实际数据库调整
        String sql = "SELECT " +
                     "    b.bookid, " +
                     "    b.barcode, " +
                     "    b.status, " +
                     "    b.shelfid, " +
                     "    b.isbn, " +
                     "    i.title, " +
                     "    s.buildingid, " +
                     "    bl.buildingname, " +
                     "    s.shelfcode, " +
                     "    s.floor, " +
                     "    s.zone " +
                     "FROM public.book b " +
                     "JOIN public.bookinfo i ON b.isbn = i.isbn " +
                     "LEFT JOIN public.bookshelf s ON b.shelfid = s.shelfid " +
                     "LEFT JOIN public.building bl ON s.buildingid = bl.buildingid " +
                     "WHERE LOWER(i.title) LIKE LOWER(?) " +
                     "ORDER BY b.bookid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword.trim() + "%";
            ps.setString(1, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                List<PhysicalBook> list = new ArrayList<>();
                while (rs.next()) {
                    PhysicalBook pb = new PhysicalBook();
                    pb.setBookId(getInteger(rs, "bookid"));
                    pb.setBarcode(nullSafe(rs.getString("barcode")));
                    pb.setStatus(nullSafe(rs.getString("status")));
                    pb.setShelfId(getInteger(rs, "shelfid"));
                    pb.setIsbn(nullSafe(rs.getString("isbn")));
                    pb.setTitle(nullSafe(rs.getString("title")));
                    pb.setBuildingId(getInteger(rs, "buildingid"));
                    pb.setBuildingName(nullSafe(rs.getString("buildingname")));
                    pb.setShelfCode(nullSafe(rs.getString("shelfcode")));
                    pb.setFloor(getInteger(rs, "floor"));
                    pb.setZone(nullSafe(rs.getString("zone")));
                    list.add(pb);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询实体书信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ISBN查询实体书信息
     */
    public List<PhysicalBook> findPhysicalBooksByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT " +
                     "    b.bookid, " +
                     "    b.barcode, " +
                     "    b.status, " +
                     "    b.shelfid, " +
                     "    b.isbn, " +
                     "    i.title, " +
                     "    s.buildingid, " +
                     "    bl.buildingname, " +
                     "    s.shelfcode, " +
                     "    s.floor, " +
                     "    s.zone " +
                     "FROM public.book b " +
                     "JOIN public.bookinfo i ON b.isbn = i.isbn " +
                     "LEFT JOIN public.bookshelf s ON b.shelfid = s.shelfid " +
                     "LEFT JOIN public.building bl ON s.buildingid = bl.buildingid " +
                     "WHERE b.isbn = ? " +
                     "ORDER BY b.bookid";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, isbn.trim());

            try (ResultSet rs = ps.executeQuery()) {
                List<PhysicalBook> list = new ArrayList<>();
                while (rs.next()) {
                    PhysicalBook pb = new PhysicalBook();
                    pb.setBookId(getInteger(rs, "bookid"));
                    pb.setBarcode(nullSafe(rs.getString("barcode")));
                    pb.setStatus(nullSafe(rs.getString("status")));
                    pb.setShelfId(getInteger(rs, "shelfid"));
                    pb.setIsbn(nullSafe(rs.getString("isbn")));
                    pb.setTitle(nullSafe(rs.getString("title")));
                    pb.setBuildingId(getInteger(rs, "buildingid"));
                    pb.setBuildingName(nullSafe(rs.getString("buildingname")));
                    pb.setShelfCode(nullSafe(rs.getString("shelfcode")));
                    pb.setFloor(getInteger(rs, "floor"));
                    pb.setZone(nullSafe(rs.getString("zone")));
                    list.add(pb);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("按ISBN查询实体书信息失败: " + e.getMessage(), e);
        }
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    private Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        Object obj = rs.getObject(columnName);
        if (obj == null || rs.wasNull()) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return null;
    }

    /**
     * 根据条形码查询实体书
     */
    public PhysicalBook findByBarcode(String barcode) {
        String sql = "SELECT b.bookid, b.barcode, b.status, b.shelfid, b.isbn, i.title " +
                     "FROM public.book b JOIN public.bookinfo i ON b.isbn = i.isbn " +
                     "WHERE b.barcode = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PhysicalBook pb = new PhysicalBook();
                    pb.setBookId(rs.getInt("bookid"));
                    pb.setBarcode(rs.getString("barcode"));
                    pb.setStatus(rs.getString("status"));
                    pb.setShelfId(getInteger(rs, "shelfid"));
                    pb.setIsbn(rs.getString("isbn"));
                    pb.setTitle(rs.getString("title"));
                    return pb;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("按条形码查询实体书失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新图书状态（在事务中执行）
     */
    public int updateStatus(Integer bookId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE public.book SET status = ? WHERE bookid = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, bookId);
            return ps.executeUpdate();
        }
    }
}
