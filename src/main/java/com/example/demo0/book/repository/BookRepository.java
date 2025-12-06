package com.example.demo0.book.repository;

import com.example.demo0.book.model.BookInfo;

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
     */
    public List<BookInfo> search(String keyword) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String base = "SELECT isbn, title, author, stock FROM public.bookinfo";
        String where = hasKeyword ? " WHERE isbn ILIKE ? OR title ILIKE ? OR author ILIKE ?" : "";
        String suffix = " ORDER BY title NULLS LAST LIMIT 200";
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

    private String nullSafe(String s) { return s == null ? "" : s; }
}
