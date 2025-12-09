package com.example.demo0.reader.service;

import com.example.demo0.book.model.PhysicalBook;
import com.example.demo0.book.repository.BookRepository;
import com.example.demo0.reader.model.BorrowRecord;
import com.example.demo0.reader.model.BorrowRecordDetail;
import com.example.demo0.reader.repository.BorrowRecordRepository;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class BorrowingService {
    private final BorrowRecordRepository borrowRecordRepository = new BorrowRecordRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final DataSource dataSource;

    public BorrowingService() {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/jdbc/LibraryDS");
        } catch (NamingException e) {
            throw new RuntimeException("JNDI 数据源未找到", e);
        }
    }

    public BorrowResult borrowBook(String readerIdStr, String barcode) {
        if (readerIdStr == null || readerIdStr.isBlank() || barcode == null || barcode.isBlank()) {
            return new BorrowResult(false, "读者ID和图书条形码不能为空", null);
        }

        Integer readerId;
        try {
            readerId = Integer.parseInt(readerIdStr);
        } catch (NumberFormatException e) {
            return new BorrowResult(false, "读者ID必须是数字", null);
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 检查图书状态
                PhysicalBook book = bookRepository.findByBarcode(barcode);
                if (book == null) {
                    conn.rollback();
                    return new BorrowResult(false, "未找到该条形码对应的图书", null);
                }
                if (!"正常".equalsIgnoreCase(book.getStatus())) {
                    conn.rollback();
                    return new BorrowResult(false, "图书状态异常，当前为 '" + book.getStatus() + "'", null);
                }

                // 2. 检查读者借阅上限
                int unreturnedCount = borrowRecordRepository.getUnreturnedCountByReader(readerId);
                if (unreturnedCount >= 5) {
                    conn.rollback();
                    return new BorrowResult(false, "已达到借阅上限（5本）", null);
                }

                // 3. 更新图书状态为 '借出'
                bookRepository.updateStatus(book.getBookId(), "借出", conn);

                // 4. 添加借阅记录
                BorrowRecord record = new BorrowRecord();
                record.setReaderId(readerId);
                record.setBookId(book.getBookId());
                record.setBorrowTime(LocalDateTime.now().atOffset(ZoneOffset.UTC));
                record.setOverdueFine(java.math.BigDecimal.ZERO);
                borrowRecordRepository.add(record, conn);

                conn.commit();
                String message = String.format("成功借阅《%s》", book.getTitle());
                return new BorrowResult(true, "图书借阅成功", message);

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("借阅操作失败: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败: " + e.getMessage(), e);
        }
    }

    public ReturnResult returnBook(String readerIdStr, String barcode) {
        if (readerIdStr == null || readerIdStr.isBlank() || barcode == null || barcode.isBlank()) {
            return new ReturnResult(false, "读者ID和图书条形码不能为空");
        }

        Integer readerId;
        try {
            readerId = Integer.parseInt(readerIdStr);
        } catch (NumberFormatException e) {
            return new ReturnResult(false, "读者ID必须是数字");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. 查找图书
                PhysicalBook book = bookRepository.findByBarcode(barcode);
                if (book == null) {
                    conn.rollback();
                    return new ReturnResult(false, "未找到该条形码对应的图书");
                }

                // 2. 查找未归还的借阅记录
                BorrowRecord record = borrowRecordRepository.findUnreturnedByReaderAndBook(readerId, book.getBookId(), conn);
                if (record == null) {
                    conn.rollback();
                    boolean hasAny = borrowRecordRepository.hasAnyRecord(readerId, book.getBookId(), conn);
                    return new ReturnResult(false, hasAny ? "该书已归还，请勿重复操作" : "未找到您的借阅记录");
                }

                // 3. 计算罚金并更新借阅记录
                Timestamp returnTime = Timestamp.valueOf(LocalDateTime.now());
                long borrowMillis = record.getBorrowTime().toInstant().toEpochMilli();
                long returnMillis = returnTime.getTime();
                long borrowDays = (returnMillis - borrowMillis) / (1000 * 60 * 60 * 24);
                java.math.BigDecimal overdueFine = java.math.BigDecimal.ZERO;
                if (borrowDays > 1) { // 借阅期限1天
                    overdueFine = java.math.BigDecimal.valueOf((borrowDays - 1) * 0.1);
                }
                borrowRecordRepository.updateReturnInfo(record.getBorrowRecordId(), returnTime, overdueFine, conn);

                // 4. 更新图书状态为 '正常'
                bookRepository.updateStatus(book.getBookId(), "正常", conn);

                conn.commit();
                return new ReturnResult(true, String.format("成功归还《%s》", book.getTitle()));

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("归还操作失败: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败: " + e.getMessage(), e);
        }
    }

    // --- 其他只读方法保持不变 ---
    public BorrowRecordDetail findById(Integer borrowRecordId) {
        return borrowRecordRepository.findById(borrowRecordId);
    }

    public List<BorrowRecordDetail> findByReaderId(String readerId) {
        try {
            return borrowRecordRepository.findByReaderId(Integer.parseInt(readerId));
        } catch (NumberFormatException e) {
            return List.of();
        }
    }

    public int getUnreturnedCountByReader(String readerId) {
        try {
            return borrowRecordRepository.getUnreturnedCountByReader(Integer.parseInt(readerId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getOverdueUnreturnedCountByReader(String readerId) {
        try {
            return borrowRecordRepository.getOverdueUnreturnedCountByReader(Integer.parseInt(readerId));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // --- 结果类 ---
    public static class BorrowResult {
        public final boolean success;
        public final String message;
        public final String data;

        public BorrowResult(boolean success, String message, String data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        // Getters... (为方便JSON序列化，可以添加)
    }

    public static class ReturnResult {
        public final boolean success;
        public final String message;

        public ReturnResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        // Getters...
    }
}
