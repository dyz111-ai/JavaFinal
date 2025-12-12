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
        System.out.println("[BookAdminRepository] ========== Repository构造函数 ==========");
        try {
            System.out.println("[BookAdminRepository] 查找 JNDI 数据源: java:/jdbc/LibraryDS");
            this.dataSource = (DataSource) new InitialContext().lookup("java:/jdbc/LibraryDS");
            System.out.println("[BookAdminRepository] ✅ 数据源获取成功");
        } catch (NamingException e) {
            System.err.println("[BookAdminRepository] ❌ JNDI 数据源查找失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JNDI 数据源查找失败", e);
        }
        System.out.println("[BookAdminRepository] ========== Repository构造函数完成 ==========");
    }

    // 搜索图书并统计副本状态
    public List<BookAdminDto> searchBooks(String search) {
        System.out.println("[BookAdminRepository] ========== 开始搜索图书 ==========");
        System.out.println("[BookAdminRepository] 搜索关键词: " + search);
        
        List<BookAdminDto> list = new ArrayList<>();
        // 查询所有book记录，不聚合，每个book显示一行
        // 关联查询：Book + BookInfo + Bookshelf 获取完整信息
        String sql = "SELECT b.bookid, b.isbn, b.barcode, b.status, " +
                "i.title, i.author, " +
                "bs.shelfcode " +
                "FROM public.book b " +
                "JOIN public.bookinfo i ON b.isbn = i.isbn " +
                "LEFT JOIN public.bookshelf bs ON b.shelfid = bs.shelfid " +
                "WHERE (? IS NULL OR ? = '' OR i.title ILIKE ? OR i.author ILIKE ? OR i.isbn ILIKE ?) " +
                "ORDER BY i.title, b.bookid";

        System.out.println("[BookAdminRepository] SQL: " + sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String term = "%" + (search == null ? "" : search.trim()) + "%";
            ps.setString(1, search); // 原始search参数用于IS NULL检查
            ps.setString(2, search); // 原始search参数用于空字符串检查
            ps.setString(3, term);   // 模糊搜索书名
            ps.setString(4, term);   // 模糊搜索作者
            ps.setString(5, term);   // 模糊搜索ISBN

            System.out.println("[BookAdminRepository] 执行查询，参数: search=" + search + ", term=" + term);

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    BookAdminDto dto = new BookAdminDto();
                    dto.setBookId(rs.getInt("bookid"));
                    dto.setIsbn(rs.getString("isbn"));
                    dto.setTitle(rs.getString("title"));
                    dto.setAuthor(rs.getString("author"));
                    dto.setBarcode(rs.getString("barcode"));
                    dto.setStatus(rs.getString("status"));
                    String shelfcode = rs.getString("shelfcode");
                    dto.setLocation(shelfcode); // 使用 shelfcode 作为位置标识
                    System.out.println("[BookAdminRepository] 图书位置: BookID=" + dto.getBookId() + ", ShelfCode=" + shelfcode);
                    dto.setPublisher(null); // BookInfo 表没有 publisher 字段
                    dto.setPublishDate(null); // BookInfo 表没有 publishdate 字段
                    
                    // 单本图书，数量相关字段设置为1或0
                    dto.setTotalCopies(1);
                    dto.setPhysicalCopies(1);
                    if ("正常".equals(dto.getStatus())) {
                        dto.setAvailableCopies(1);
                        dto.setBorrowedCopies(0);
                        dto.setTakedownCopies(0);
                    } else if ("借出".equals(dto.getStatus())) {
                        dto.setAvailableCopies(0);
                        dto.setBorrowedCopies(1);
                        dto.setTakedownCopies(0);
                    } else if ("下架".equals(dto.getStatus())) {
                        dto.setAvailableCopies(0);
                        dto.setBorrowedCopies(0);
                        dto.setTakedownCopies(1);
                    } else {
                        dto.setAvailableCopies(0);
                        dto.setBorrowedCopies(0);
                        dto.setTakedownCopies(0);
                    }

                    System.out.println("[BookAdminRepository] 找到图书: BookID=" + dto.getBookId() + 
                                     ", ISBN=" + dto.getIsbn() + 
                                     ", Title=" + dto.getTitle() + 
                                     ", Barcode=" + dto.getBarcode() +
                                     ", Status=" + dto.getStatus());
                    
                    list.add(dto);
                }
                System.out.println("[BookAdminRepository] 查询完成，共找到 " + count + " 条记录");
            }
        } catch (SQLException e) {
            System.err.println("[BookAdminRepository] ❌ SQL异常: " + e.getMessage());
            System.err.println("[BookAdminRepository] SQL状态: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[BookAdminRepository] ❌ 其他异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[BookAdminRepository] 返回结果数量: " + list.size());
        System.out.println("[BookAdminRepository] ========== 搜索图书完成 ==========");
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
        System.out.println("[BookAdminRepository] ========== 开始添加图书副本 ==========");
        System.out.println("[BookAdminRepository] ISBN: " + dto.getIsbn());
        System.out.println("[BookAdminRepository] 数量: " + dto.getNumberOfCopies());
        System.out.println("[BookAdminRepository] BuildingID: " + dto.getBuildingId());
        System.out.println("[BookAdminRepository] Floor: " + dto.getFloor());
        System.out.println("[BookAdminRepository] Zone: " + dto.getZone());
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 根据 buildingid, floor, zone 查找 shelfid
                String findShelfSql = "SELECT shelfid FROM public.bookshelf WHERE buildingid = ? AND floor = ? AND zone = ? LIMIT 1";
                Integer shelfId = null;
                try (PreparedStatement ps = conn.prepareStatement(findShelfSql)) {
                    ps.setInt(1, dto.getBuildingId());
                    ps.setInt(2, dto.getFloor());
                    ps.setString(3, dto.getZone());
                    System.out.println("[BookAdminRepository] 查找书架SQL: " + findShelfSql);
                    System.out.println("[BookAdminRepository] 参数: buildingid=" + dto.getBuildingId() + ", floor=" + dto.getFloor() + ", zone=" + dto.getZone());
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            shelfId = rs.getInt("shelfid");
                            System.out.println("[BookAdminRepository] ✅ 找到书架，ShelfID: " + shelfId);
                        } else {
                            System.out.println("[BookAdminRepository] ❌ 未找到匹配的书架");
                            throw new RuntimeException("未找到匹配的书架，请检查楼宇、楼层、区域信息");
                        }
                    }
                }

                // 2. 获取当前最大后缀
                String maxSql = "SELECT COUNT(*) FROM public.book WHERE isbn = ?";
                int currentCount = 0;
                try (PreparedStatement ps = conn.prepareStatement(maxSql)) {
                    ps.setString(1, dto.getIsbn());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) currentCount = rs.getInt(1);
                    }
                }

                // 3. 插入新副本
                String insertSql = "INSERT INTO public.book (isbn, barcode, status, shelfid) VALUES (?, ?, ?, ?)";
                System.out.println("[BookAdminRepository] 插入图书SQL: " + insertSql);
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (int i = 0; i < dto.getNumberOfCopies(); i++) {
                        ps.setString(1, dto.getIsbn());
                        ps.setString(2, dto.getIsbn() + "-" + (currentCount + i + 1));
                        ps.setString(3, "正常");
                        ps.setInt(4, shelfId);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    System.out.println("[BookAdminRepository] ✅ 成功插入 " + dto.getNumberOfCopies() + " 本图书副本");
                }

                // 4. 更新 BookInfo 库存统计
                String updateStock = "UPDATE public.bookinfo SET stock = stock + ? WHERE isbn = ?";
                try(PreparedStatement ps = conn.prepareStatement(updateStock)) {
                    ps.setInt(1, dto.getNumberOfCopies());
                    ps.setString(2, dto.getIsbn());
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("[BookAdminRepository] ✅ 事务提交成功");
                System.out.println("[BookAdminRepository] ========== 添加图书副本完成 ==========");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[BookAdminRepository] ❌ 事务回滚: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("添加副本失败: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                conn.rollback();
                System.err.println("[BookAdminRepository] ❌ 事务回滚: " + e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[BookAdminRepository] ❌ 数据库连接错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库连接错误: " + e.getMessage(), e);
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

    // 更新位置（根据bookId和位置信息更新shelfid）
    public boolean updateBookLocation(Integer bookId, Integer buildingId, Integer floor, String zone) {
        System.out.println("[BookAdminRepository] ========== 开始更新图书位置 ==========");
        System.out.println("[BookAdminRepository] BookID: " + bookId);
        System.out.println("[BookAdminRepository] BuildingID: " + buildingId);
        System.out.println("[BookAdminRepository] Floor: " + floor);
        System.out.println("[BookAdminRepository] Zone: " + zone);
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 根据 buildingid, floor, zone 查找 shelfid
                // 注意：PostgreSQL表名可能是大小写敏感的，尝试两种写法
                String findShelfSql = "SELECT shelfid, shelfcode FROM public.bookshelf WHERE buildingid = ? AND floor = ? AND zone = ? LIMIT 1";
                Integer shelfId = null;
                String shelfCode = null;
                try (PreparedStatement ps = conn.prepareStatement(findShelfSql)) {
                    ps.setInt(1, buildingId);
                    ps.setInt(2, floor);
                    ps.setString(3, zone);
                    System.out.println("[BookAdminRepository] 查找书架SQL: " + findShelfSql);
                    System.out.println("[BookAdminRepository] 参数: buildingid=" + buildingId + ", floor=" + floor + ", zone='" + zone + "'");
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            shelfId = rs.getInt("shelfid");
                            shelfCode = rs.getString("shelfcode");
                            System.out.println("[BookAdminRepository] ✅ 找到书架，ShelfID: " + shelfId + ", ShelfCode: " + shelfCode);
                        } else {
                            // 如果找不到，尝试查询所有匹配的记录看看是否有数据
                            String debugSql = "SELECT shelfid, shelfcode, buildingid, floor, zone FROM public.bookshelf WHERE buildingid = ?";
                            try (PreparedStatement debugPs = conn.prepareStatement(debugSql)) {
                                debugPs.setInt(1, buildingId);
                                try (ResultSet debugRs = debugPs.executeQuery()) {
                                    System.out.println("[BookAdminRepository] 调试：查询buildingid=" + buildingId + "的所有书架:");
                                    int count = 0;
                                    while (debugRs.next()) {
                                        count++;
                                        System.out.println("[BookAdminRepository]   书架" + count + ": shelfid=" + debugRs.getInt("shelfid") + 
                                                         ", shelfcode=" + debugRs.getString("shelfcode") + 
                                                         ", floor=" + debugRs.getInt("floor") + 
                                                         ", zone='" + debugRs.getString("zone") + "'");
                                    }
                                    if (count == 0) {
                                        System.out.println("[BookAdminRepository]   该楼宇下没有任何书架记录");
                                    }
                                }
                            }
                            System.out.println("[BookAdminRepository] ❌ 未找到匹配的书架 (buildingid=" + buildingId + ", floor=" + floor + ", zone='" + zone + "')");
                            throw new RuntimeException("未找到匹配的书架，请检查楼宇、楼层、区域信息。可能数据库中不存在该位置的书架记录。");
                        }
                    }
                }

                // 2. 更新book表的shelfid
                String updateSql = "UPDATE public.book SET shelfid = ? WHERE bookid = ?";
                System.out.println("[BookAdminRepository] 更新图书SQL: " + updateSql);
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, shelfId);
                    ps.setInt(2, bookId);
                    int affected = ps.executeUpdate();
                    System.out.println("[BookAdminRepository] ✅ 更新成功，影响行数: " + affected);
                    
                    if (affected > 0) {
                        conn.commit();
                        System.out.println("[BookAdminRepository] ========== 更新图书位置完成 ==========");
                        return true;
                    } else {
                        conn.rollback();
                        System.out.println("[BookAdminRepository] ❌ 未找到要更新的图书");
                        return false;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("[BookAdminRepository] ❌ SQL异常: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("更新位置失败: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[BookAdminRepository] ❌ 数据库连接错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 下架图书（根据bookId）
    public boolean takedownBook(Integer bookId) {
        System.out.println("[BookAdminRepository] ========== 开始下架图书 ==========");
        System.out.println("[BookAdminRepository] BookID: " + bookId);
        
        String sql = "UPDATE public.book SET status='下架' WHERE bookid=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            int affected = ps.executeUpdate();
            System.out.println("[BookAdminRepository] ✅ 下架成功，影响行数: " + affected);
            System.out.println("[BookAdminRepository] ========== 下架图书完成 ==========");
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("[BookAdminRepository] ❌ 下架失败: " + e.getMessage());
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