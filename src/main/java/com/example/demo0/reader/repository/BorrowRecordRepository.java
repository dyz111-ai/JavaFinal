package com.example.demo0.reader.repository;

import com.example.demo0.reader.model.BorrowRecord;
import com.example.demo0.reader.model.BorrowRecordDetail;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * 借阅记录数据访问层
 */
public class BorrowRecordRepository {
    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";

    private final DataSource dataSource;

    public BorrowRecordRepository() {
        this.dataSource = lookup();
    }

    private DataSource lookup() {
        try {
            return (DataSource) new InitialContext().lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException("JNDI 数据源未找到: " + JNDI_NAME, e);
        }
    }

    /**
     * 根据借阅记录ID获取借阅记录详情
     */
    public BorrowRecordDetail findById(Integer borrowRecordId) {
        String sql = "SELECT br.BorrowRecordID, br.BookID, bi.ISBN, bi.Title as BookTitle, bi.Author as BookAuthor, " +
                     "br.ReaderID, COALESCE(r.Fullname, r.Nickname, '') as ReaderName, br.BorrowTime, br.ReturnTime, br.OverdueFine " +
                     "FROM public.BorrowRecord br " +
                     "LEFT JOIN public.Book b ON br.BookID = b.BookID " +
                     "LEFT JOIN public.BookInfo bi ON b.ISBN = bi.ISBN " +
                     "LEFT JOIN public.Reader r ON br.ReaderID = r.ReaderID " +
                     "WHERE br.BorrowRecordID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowRecordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDetail(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据读者ID获取该读者的所有借阅记录
     */
    public List<BorrowRecordDetail> findByReaderId(Integer readerId) {
        String sql = "SELECT br.BorrowRecordID, br.BookID, bi.ISBN, bi.Title as BookTitle, bi.Author as BookAuthor, " +
                     "br.ReaderID, COALESCE(r.Fullname, r.Nickname, '') as ReaderName, br.BorrowTime, br.ReturnTime, br.OverdueFine " +
                     "FROM public.BorrowRecord br " +
                     "LEFT JOIN public.Book b ON br.BookID = b.BookID " +
                     "LEFT JOIN public.BookInfo bi ON b.ISBN = bi.ISBN " +
                     "LEFT JOIN public.Reader r ON br.ReaderID = r.ReaderID " +
                     "WHERE br.ReaderID = ? " +
                     "ORDER BY br.BorrowTime DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<BorrowRecordDetail> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapDetail(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询读者借阅记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 添加新的借阅记录
     */
    public int add(BorrowRecord record, Connection conn) throws SQLException {
        String sql = "INSERT INTO public.BorrowRecord (ReaderID, BookID, BorrowTime, ReturnTime, OverdueFine) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, record.getReaderId());
            ps.setInt(2, record.getBookId());
            Timestamp borrowTime = record.getBorrowTime() != null
                ? Timestamp.from(record.getBorrowTime().toInstant())
                : Timestamp.valueOf(LocalDateTime.now());
            ps.setTimestamp(3, borrowTime);
            ps.setTimestamp(4, record.getReturnTime() != null
                ? Timestamp.from(record.getReturnTime().toInstant())
                : null);
            ps.setBigDecimal(5, record.getOverdueFine());
            return ps.executeUpdate();
        }
    }

    /**
     * 查找读者某本未归还的图书记录（在事务中执行）
     */
    public BorrowRecord findUnreturnedByReaderAndBook(Integer readerId, Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM public.BorrowRecord WHERE ReaderID = ? AND BookID = ? AND ReturnTime IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BorrowRecord record = new BorrowRecord();
                    record.setBorrowRecordId(rs.getInt("BorrowRecordID"));
                    record.setReaderId(rs.getInt("ReaderID"));
                    record.setBookId(rs.getInt("BookID"));
                    record.setBorrowTime(rs.getTimestamp("BorrowTime"));
                    record.setOverdueFine(rs.getBigDecimal("OverdueFine"));
                    return record;
                }
                return null;
            }
        }
    }

    /**
     * 更新归还信息（在事务中执行）
     */
    public int updateReturnInfo(Integer borrowRecordId, Timestamp returnTime, java.math.BigDecimal overdueFine, Connection conn) throws SQLException {
        String sql = "UPDATE public.BorrowRecord SET ReturnTime = ?, OverdueFine = ? WHERE BorrowRecordID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, returnTime);
            ps.setBigDecimal(2, overdueFine);
            ps.setInt(3, borrowRecordId);
            return ps.executeUpdate();
        }
    }

    /**
     * 检查是否存在任意借阅记录（无论是否归还，在事务中执行）
     */
    public boolean hasAnyRecord(Integer readerId, Integer bookId, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM public.BorrowRecord WHERE ReaderID = ? AND BookID = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * 获取读者未归还的图书数量
     */
    public int getUnreturnedCountByReader(Integer readerId) {
        String sql = "SELECT COUNT(*) FROM public.BorrowRecord WHERE ReaderID = ? AND ReturnTime IS NULL";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询未归还数量失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取读者未归还且逾期的图书数量
     * 假设借阅期限为30天
     */
    public int getOverdueUnreturnedCountByReader(Integer readerId) {
        String sql = "SELECT COUNT(*) FROM public.BorrowRecord " +
                     "WHERE ReaderID = ? AND ReturnTime IS NULL " +
                     "AND BorrowTime < CURRENT_TIMESTAMP - INTERVAL '30 days'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询逾期数量失败: " + e.getMessage(), e);
        }
    }

    private BorrowRecordDetail mapDetail(ResultSet rs) throws SQLException {
        BorrowRecordDetail detail = new BorrowRecordDetail();
        detail.setBorrowRecordId(rs.getObject("BorrowRecordID") == null ? null : rs.getInt("BorrowRecordID"));
        detail.setBookId(String.valueOf(rs.getInt("BookID")));
        detail.setIsbn(rs.getString("ISBN"));
        detail.setBookTitle(rs.getString("BookTitle"));
        detail.setBookAuthor(rs.getString("BookAuthor"));
        detail.setReaderId(String.valueOf(rs.getInt("ReaderID")));
        detail.setReaderName(rs.getString("ReaderName"));
        detail.setBorrowTime(rs.getTimestamp("BorrowTime"));
        detail.setReturnTime(rs.getTimestamp("ReturnTime"));
        detail.setOverdueFine(rs.getBigDecimal("OverdueFine"));
        return detail;
    }

    /**
     * 获取读者借阅过的所有书籍的作者和分类信息，用于推荐分析
     */
    public List<java.util.Map<String, String>> findBorrowedBookAnalyticsData(Integer readerId) {
        List<java.util.Map<String, String>> data = new ArrayList<>();
        String sql = "SELECT bi.author, c.categoryname " +
                     "FROM public.borrowrecord br " +
                     "JOIN public.book b ON br.bookid = b.bookid " +
                     "JOIN public.bookinfo bi ON b.isbn = bi.isbn " +
                     "LEFT JOIN public.book_classify bc ON bi.isbn = bc.isbn " +
                     "LEFT JOIN public.category c ON bc.categoryid = c.categoryid " +
                     "WHERE br.readerid = ? AND bi.author IS NOT NULL AND c.categoryname IS NOT NULL";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, String> row = new java.util.HashMap<>();
                    row.put("author", rs.getString("author"));
                    row.put("category", rs.getString("categoryname"));
                    data.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询读者借阅分析数据失败: " + e.getMessage(), e);
        }
        return data;
    }
}

