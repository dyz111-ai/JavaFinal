package com.example.demo0.admin.repository;

import com.example.demo0.admin.dto.AddCopiesDto;
import com.example.demo0.admin.dto.BookAdminDto;
import com.example.demo0.admin.dto.CreateBookDto;
import com.example.demo0.admin.dto.UpdateBookDto;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookAdminRepository {

    private final DataSource dataSource;

    public BookAdminRepository() {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/jdbc/LibraryDS");
        } catch (NamingException e) {
            throw new RuntimeException("JNDI 数据源查找失败", e);
        }
    }

    // 搜索图书并统计副本状态
    public List<BookAdminDto> searchBooks(String search) {
        List<BookAdminDto> list = new ArrayList<>();
        // 关联查询：BookInfo + 统计 Book 表中的各状态数量 + 获取一个位置样本
        String sql = "SELECT i.isbn, i.title, i.author, " +
                "COUNT(b.bookid) as total_physical, " +
                "COUNT(CASE WHEN b.status = '正常' THEN 1 END) as available, " +
                "COUNT(CASE WHEN b.status = '借出' THEN 1 END) as borrowed, " +
                "COUNT(CASE WHEN b.status = '下架' THEN 1 END) as takedown, " +
                "(SELECT location FROM public.book WHERE isbn = i.isbn LIMIT 1) as sample_location " +
                "FROM public.bookinfo i " +
                "LEFT JOIN public.book b ON i.isbn = b.isbn " +
                "WHERE i.title ILIKE ? OR i.author ILIKE ? OR i.isbn ILIKE ? " +
                "GROUP BY i.isbn, i.title, i.author " +
                "ORDER BY i.title";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String term = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookAdminDto dto = new BookAdminDto();
                    dto.setIsbn(rs.getString("isbn"));
                    dto.setTitle(rs.getString("title"));
                    dto.setAuthor(rs.getString("author"));
                    dto.setLocation(rs.getString("sample_location")); // 获取位置

                    int total = rs.getInt("total_physical");
                    dto.setTotalCopies(total);
                    dto.setPhysicalCopies(total);
                    dto.setAvailableCopies(rs.getInt("available"));
                    dto.setBorrowedCopies(rs.getInt("borrowed"));
                    dto.setTakedownCopies(rs.getInt("takedown"));

                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 创建图书 (事务：BookInfo + Multiple Books)
    public void createBook(CreateBookDto dto) {
        if (isIsbnExists(dto.getIsbn())) {
            throw new RuntimeException("ISBN 已存在: " + dto.getIsbn());
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 插入 BookInfo
                String sqlInfo = "INSERT INTO public.bookinfo (isbn, title, author, stock) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInfo)) {
                    ps.setString(1, dto.getIsbn());
                    ps.setString(2, dto.getTitle());
                    ps.setString(3, dto.getAuthor());
                    ps.setInt(4, dto.getNumberOfCopies());
                    ps.executeUpdate();
                }

                // 2. 插入 Book 副本
                String sqlBook = "INSERT INTO public.book (isbn, barcode, status) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlBook)) {
                    for (int i = 0; i < dto.getNumberOfCopies(); i++) {
                        ps.setString(1, dto.getIsbn());
                        ps.setString(2, dto.getIsbn() + "-" + (i + 1));
                        ps.setString(3, "正常");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("创建图书失败: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库错误", e);
        }
    }

    // 添加副本
    public void addCopies(AddCopiesDto dto) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 获取当前最大后缀
                String maxSql = "SELECT COUNT(*) FROM public.book WHERE isbn = ?";
                int currentCount = 0;
                try (PreparedStatement ps = conn.prepareStatement(maxSql)) {
                    ps.setString(1, dto.getIsbn());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) currentCount = rs.getInt(1);
                    }
                }

                // 2. 插入新副本
                String insertSql = "INSERT INTO public.book (isbn, barcode, status, shelfid) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < dto.getNumberOfCopies(); i++) {
                        ps.setString(1, dto.getIsbn());
                        ps.setString(2, dto.getIsbn() + "-" + (currentCount + i + 1));
                        ps.setString(3, "正常");
                        if (dto.getShelfId() != null) {
                            ps.setInt(4, dto.getShelfId());
                        } else {
                            ps.setNull(4, Types.INTEGER);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // 3. 更新 BookInfo 库存统计
                String updateStock = "UPDATE public.bookinfo SET stock = stock + ? WHERE isbn = ?";
                try(PreparedStatement ps = conn.prepareStatement(updateStock)) {
                    ps.setInt(1, dto.getNumberOfCopies());
                    ps.setString(2, dto.getIsbn());
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("添加副本失败", e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新图书信息
    public boolean updateBookInfo(String isbn, UpdateBookDto dto) {
        String sql = "UPDATE public.bookinfo SET title=?, author=? WHERE isbn=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getTitle());
            ps.setString(2, dto.getAuthor());
            ps.setString(3, isbn);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新位置
    public boolean updateBookLocation(String isbn, String location) {
        // 假设 Book 表有 location 字段（根据前端需求）
        // 如果数据库没有 location 字段，这步会报错，请确保数据库结构匹配
        String sql = "UPDATE public.book SET location=? WHERE isbn=? AND status='正常'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location);
            ps.setString(2, isbn);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新位置失败 (可能数据库缺少location字段): " + e.getMessage());
            return false;
        }
    }

    // 下架图书
    public boolean takedownBook(String isbn) {
        String sql = "UPDATE public.book SET status='下架' WHERE isbn=? AND status='正常'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isIsbnExists(String isbn) {
        String sql = "SELECT 1 FROM public.bookinfo WHERE isbn = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}