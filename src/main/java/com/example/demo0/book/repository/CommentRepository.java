package com.example.demo0.book.repository;

import com.example.demo0.book.model.CommentRecord;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentRepository {
    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";
    // 为兼容现有表结构（可能为 varchar(20)），做长度保护
    private static final int MAX_VARCHAR_LEN = 20;

    private final DataSource dataSource;

    public CommentRepository() {
        this.dataSource = lookup();
    }

    private DataSource lookup() {
        try {
            return (DataSource) new InitialContext().lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException("JNDI 数据源未找到: " + JNDI_NAME, e);
        }
    }

    public List<CommentRecord> findByIsbn(String isbn, int limit) {
        if (isbn == null || isbn.isBlank()) return List.of();
        String sql = "SELECT readerid, isbn, rating, reviewcontent, createtime, status " +
                     "FROM public.comment_table WHERE lower(regexp_replace(trim(isbn), '[^0-9x]', '', 'g')) = lower(regexp_replace(trim(?), '[^0-9x]', '', 'g')) ORDER BY createtime DESC LIMIT ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            ps.setInt(2, Math.max(limit, 1));
            try (ResultSet rs = ps.executeQuery()) {
                List<CommentRecord> list = new ArrayList<>();
                while (rs.next()) {
                    CommentRecord c = map(rs);
                    list.add(c);
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询评论失败: " + e.getMessage(), e);
        }
    }

    public List<CommentRecord> findById(long id) {
        String sql = "SELECT readerid, isbn, rating, reviewcontent, createtime, status " +
                     "FROM public.comment_table WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                List<CommentRecord> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("按ID查询评论失败: " + e.getMessage(), e);
        }
    }

    public int addComment(long readerId, String isbn, Short rating, String content, String status) {
        String sql = "INSERT INTO public.comment_table (readerid, isbn, rating, reviewcontent, createtime, status) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String isbnSafe = safeVarchar(isbn);
            String contentSafe = safeVarchar(content);
            String statusSafe = safeVarchar(status == null ? "正常" : status);
            short ratingSafe = normalizeRating(rating);

            ps.setLong(1, readerId);
            ps.setString(2, isbnSafe);
            ps.setShort(3, ratingSafe);
            ps.setString(4, contentSafe);
            ps.setString(5, statusSafe);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("新增评论失败: " + e.getMessage(), e);
        }
    }

    private short normalizeRating(Short r) {
        if (r == null) return 5; // 默认好评
        int x = Math.max(1, Math.min(5, r));
        return (short) x;
    }

    private String safeVarchar(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.length() > MAX_VARCHAR_LEN) {
            return t.substring(0, MAX_VARCHAR_LEN);
        }
        return t;
    }

    private CommentRecord map(ResultSet rs) throws SQLException {
        CommentRecord c = new CommentRecord();
        c.setReaderId(rs.getObject("readerid") == null ? null : rs.getLong("readerid"));
        c.setIsbn(rs.getString("isbn"));
        c.setRating(rs.getObject("rating") == null ? null : rs.getShort("rating"));
        Timestamp ts = rs.getTimestamp("createtime");
        c.setCreateTime(ts);
        c.setReviewContent(rs.getString("reviewcontent"));
        c.setStatus(rs.getString("status"));
        return c;
    }
}
