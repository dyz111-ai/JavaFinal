package com.example.demo0.reader.repository;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.reader.model.BookList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookListRepository {
    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";
    private final DataSource dataSource;

    public BookListRepository() {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException("JNDI 数据源未找到: " + JNDI_NAME, e);
        }
    }

    public BookList createBookList(BookList booklist) throws SQLException {
        String sql = "INSERT INTO public.booklist (listcode, booklistname, booklistintroduction, creatorid) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"booklistid"})) {
            ps.setString(1, booklist.getListCode());
            ps.setString(2, booklist.getBooklistName());
            ps.setString(3, booklist.getBooklistIntroduction());
            ps.setInt(4, booklist.getCreatorId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booklist.setBooklistId(generatedKeys.getInt(1));
                }
            }
            return booklist;
        }
    }

    /**
     * 查询读者创建的书单
     */
    public List<BookList> findCreatedBooklistsByReaderId(Integer readerId) throws SQLException {
        List<BookList> booklists = new ArrayList<>();
        String sql = "SELECT b.* FROM public.booklist b WHERE b.creatorid = ? ORDER BY b.booklistid DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    booklists.add(mapToBookList(rs));
                }
            }
        }
        return booklists;
    }

    /**
     * 查询读者收藏的书单
     */
    public List<BookList> findCollectedBooklistsByReaderId(Integer readerId) throws SQLException {
        List<BookList> booklists = new ArrayList<>();
        String sql = "SELECT b.*, c.favoritetime, c.notes " +
                     "FROM public.booklist b " +
                     "JOIN public.collect c ON b.booklistid = c.booklistid " +
                     "WHERE c.readerid = ? " +
                     "ORDER BY c.favoritetime DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookList booklist = mapToBookList(rs);
                    Timestamp favoriteTime = rs.getTimestamp("favoritetime");
                    if (favoriteTime != null) {
                        booklist.setFavoriteTime(favoriteTime);
                    }
                    // 处理收藏备注（可能是CLOB类型）
                    String notes = null;
                    try {
                        java.sql.Clob clob = rs.getClob("notes");
                        if (clob != null) {
                            long length = clob.length();
                            if (length > 0) {
                                notes = clob.getSubString(1, (int) length);
                            }
                        }
                    } catch (SQLException e) {
                        notes = rs.getString("notes");
                    }
                    booklist.setNotes(notes);
                    booklists.add(booklist);
                }
            }
        }
        return booklists;
    }

    public BookList findBookListById(Integer booklistId) throws SQLException {
        String sql = "SELECT * FROM public.booklist WHERE booklistid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapToBookList(rs);
                }
                return null;
            }
        }
    }

    public List<BookInfo> findBooksInBookList(Integer booklistId) throws SQLException {
        List<BookInfo> books = new ArrayList<>();
        String sql = "SELECT bi.* FROM public.bookinfo bi " +
                     "JOIN public.booklist_book bb ON bi.isbn = bb.isbn " +
                     "WHERE bb.booklistid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookInfo book = new BookInfo();
                    book.setISBN(rs.getString("isbn"));
                    book.setTitle(rs.getString("title"));
                    book.setAuthor(rs.getString("author"));
                    book.setStock(rs.getInt("stock"));
                    books.add(book);
                }
            }
        }
        return books;
    }

    private BookList mapToBookList(ResultSet rs) throws SQLException {
        BookList booklist = new BookList();
        booklist.setBooklistId(rs.getInt("booklistid"));
        booklist.setListCode(rs.getString("listcode"));
        booklist.setBooklistName(rs.getString("booklistname"));
        // 处理 CLOB 类型字段
        String intro = null;
        try {
            java.sql.Clob clob = rs.getClob("booklistintroduction");
            if (clob != null) {
                long length = clob.length();
                if (length > 0) {
                    intro = clob.getSubString(1, (int) length);
                }
            }
        } catch (SQLException e) {
            // 如果不是 CLOB，尝试作为字符串读取
            intro = rs.getString("booklistintroduction");
        }
        booklist.setBooklistIntroduction(intro);
        booklist.setCreatorId(rs.getInt("creatorid"));
        return booklist;
    }

    public int deleteBookList(Integer booklistId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 先删除关联表中的数据
                try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM public.booklist_book WHERE booklistid = ?")) {
                    ps1.setInt(1, booklistId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM public.collect WHERE booklistid = ?")) {
                    ps2.setInt(1, booklistId);
                    ps2.executeUpdate();
                }
                // 最后删除主表
                try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM public.booklist WHERE booklistid = ?")) {
                    ps3.setInt(1, booklistId);
                    int result = ps3.executeUpdate();
                    conn.commit();
                    return result;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public int addBookToBookList(Integer booklistId, String isbn) throws SQLException {
        String sql = "INSERT INTO public.booklist_book (booklistid, isbn, addtime) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            ps.setString(2, isbn);
            return ps.executeUpdate();
        }
    }

    public int removeBookFromBookList(Integer booklistId, String isbn) throws SQLException {
        String sql = "DELETE FROM public.booklist_book WHERE booklistid = ? AND isbn = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            ps.setString(2, isbn);
            return ps.executeUpdate();
        }
    }

    public int updateBookListInfo(Integer booklistId, String name, String introduction) throws SQLException {
        String sql = "UPDATE public.booklist SET booklistname = ?, booklistintroduction = ? WHERE booklistid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, introduction);
            ps.setInt(3, booklistId);
            return ps.executeUpdate();
        }
    }

    /**
     * 检查书单是否存在且属于指定读者
     */
    public boolean checkBooklistOwnership(Integer booklistId, Integer readerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM public.booklist WHERE booklistid = ? AND creatorid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            ps.setInt(2, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * 推荐相似书单（基于共同书籍数量）
     */
    public List<BookList> recommendBooklists(Integer booklistId, Integer limit) throws SQLException {
        List<BookList> recommendations = new ArrayList<>();
        String sql = "WITH CurrentBooklist AS (" +
                     "    SELECT isbn FROM public.booklist_book WHERE booklistid = ?" +
                     ")," +
                     "OtherBooklists AS (" +
                     "    SELECT b.booklistid, b.listcode, b.booklistname, b.booklistintroduction, " +
                     "           b.creatorid, COUNT(bb.isbn) AS totalbooks, " +
                     "           COUNT(CASE WHEN bb.isbn IN (SELECT isbn FROM CurrentBooklist) THEN 1 END) AS commonbooks " +
                     "    FROM public.booklist b " +
                     "    JOIN public.booklist_book bb ON b.booklistid = bb.booklistid " +
                     "    WHERE b.booklistid != ? " +
                     "    GROUP BY b.booklistid, b.listcode, b.booklistname, b.booklistintroduction, b.creatorid" +
                     ")" +
                     "SELECT ob.booklistid, ob.listcode, ob.booklistname, ob.booklistintroduction, " +
                     "       ob.creatorid, ob.commonbooks AS matchingbookscount " +
                     "FROM OtherBooklists ob " +
                     "WHERE ob.commonbooks > 0 " +
                     "ORDER BY ob.commonbooks DESC, ob.totalbooks DESC " +
                     "LIMIT ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            ps.setInt(2, booklistId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookList booklist = mapToBookList(rs);
                    recommendations.add(booklist);
                }
            }
        }
        return recommendations;
    }

    /**
     * 收藏书单
     */
    public int collectBooklist(Integer booklistId, Integer readerId, String notes) throws SQLException {
        // 先检查是否已收藏
        String checkSql = "SELECT COUNT(*) FROM public.collect WHERE booklistid = ? AND readerid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, booklistId);
            checkPs.setInt(2, readerId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return 0; // 已收藏
                }
            }
        }

        // 检查书单是否存在
        String existsSql = "SELECT COUNT(*) FROM public.booklist WHERE booklistid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement existsPs = conn.prepareStatement(existsSql)) {
            existsPs.setInt(1, booklistId);
            try (ResultSet rs = existsPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    return 0; // 书单不存在
                }
            }
        }

        // 插入收藏记录
        String sql = "INSERT INTO public.collect (booklistid, readerid, favoritetime, notes) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            ps.setInt(2, readerId);
            ps.setString(3, notes);
            return ps.executeUpdate();
        }
    }

    /**
     * 取消收藏书单
     */
    public int cancelCollectBooklist(Integer booklistId, Integer readerId) throws SQLException {
        String sql = "DELETE FROM public.collect WHERE booklistid = ? AND readerid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booklistId);
            ps.setInt(2, readerId);
            return ps.executeUpdate();
        }
    }

    /**
     * 更新收藏备注
     */
    public int updateCollectNotes(Integer booklistId, Integer readerId, String newNotes) throws SQLException {
        String sql = "UPDATE public.collect SET notes = ? WHERE booklistid = ? AND readerid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newNotes);
            ps.setInt(2, booklistId);
            ps.setInt(3, readerId);
            return ps.executeUpdate();
        }
    }

    /**
     * 只更新书单名称
     */
    public int updateBooklistName(Integer booklistId, String name, Integer readerId) throws SQLException {
        String sql = "UPDATE public.booklist SET booklistname = ? WHERE booklistid = ? AND creatorid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, booklistId);
            ps.setInt(3, readerId);
            return ps.executeUpdate();
        }
    }

    /**
     * 只更新书单简介
     */
    public int updateBooklistIntroduction(Integer booklistId, String introduction, Integer readerId) throws SQLException {
        String sql = "UPDATE public.booklist SET booklistintroduction = ? WHERE booklistid = ? AND creatorid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, introduction);
            ps.setInt(2, booklistId);
            ps.setInt(3, readerId);
            return ps.executeUpdate();
        }
    }
}

