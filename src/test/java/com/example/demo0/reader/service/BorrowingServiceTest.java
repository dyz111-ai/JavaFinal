package com.example.demo0.reader.service;

import com.example.demo0.book.model.PhysicalBook;
import com.example.demo0.book.repository.BookRepository;
import com.example.demo0.reader.model.BorrowRecord;
import com.example.demo0.reader.repository.BorrowRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BorrowingService 单元测试
 * 使用Mock隔离仓储层和数据源依赖
 */
class BorrowingServiceTest {

    private BorrowingService service;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用反射绕过构造函数
        service = allocateWithoutConstructor();
        
        // 设置模拟的依赖
        Field repoField = BorrowingService.class.getDeclaredField("borrowRecordRepository");
        repoField.setAccessible(true);
        repoField.set(service, borrowRecordRepository);

        Field bookRepoField = BorrowingService.class.getDeclaredField("bookRepository");
        bookRepoField.setAccessible(true);
        bookRepoField.set(service, bookRepository);

        Field dataSourceField = BorrowingService.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(service, dataSource);
    }

    @Test
    void borrowBook_shouldReturnFailureWhenParametersAreNull() {
        BorrowingService.BorrowResult result = service.borrowBook(null, null);
        assertFalse(result.success);
        assertEquals("读者ID和图书条形码不能为空", result.message);
    }

    @Test
    void borrowBook_shouldReturnFailureWhenReaderIdIsNotNumeric() {
        BorrowingService.BorrowResult result = service.borrowBook("abc", "123456");
        assertFalse(result.success);
        assertEquals("读者ID必须是数字", result.message);
    }

    @Test
    void borrowBook_shouldReturnFailureWhenBookNotFound() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(bookRepository.findByBarcode("123456")).thenReturn(null);
        
        BorrowingService.BorrowResult result = service.borrowBook("1", "123456");
        
        assertFalse(result.success);
        assertEquals("未找到该条形码对应的图书", result.message);
        verify(connection).rollback();
    }

    @Test
    void borrowBook_shouldReturnFailureWhenBookStatusIsNotNormal() throws SQLException {
        PhysicalBook book = new PhysicalBook();
        book.setBookId(1);
        book.setBarcode("123456");
        book.setStatus("借出");
        book.setTitle("测试图书");
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(bookRepository.findByBarcode("123456")).thenReturn(book);
        
        BorrowingService.BorrowResult result = service.borrowBook("1", "123456");
        
        assertFalse(result.success);
        assertEquals("图书状态异常，当前为 '借出'", result.message);
        verify(connection).rollback();
    }

    @Test
    void borrowBook_shouldReturnFailureWhenBorrowLimitExceeded() throws SQLException {
        PhysicalBook book = new PhysicalBook();
        book.setBookId(1);
        book.setBarcode("123456");
        book.setStatus("正常");
        book.setTitle("测试图书");
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(bookRepository.findByBarcode("123456")).thenReturn(book);
        when(borrowRecordRepository.getUnreturnedCountByReader(1)).thenReturn(5);
        
        BorrowingService.BorrowResult result = service.borrowBook("1", "123456");
        
        assertFalse(result.success);
        assertEquals("已达到借阅上限（5本）", result.message);
        verify(connection).rollback();
    }

    @Test
    void borrowBook_shouldReturnSuccessWhenAllConditionsMet() throws SQLException {
        PhysicalBook book = new PhysicalBook();
        book.setBookId(1);
        book.setBarcode("123456");
        book.setStatus("正常");
        book.setTitle("测试图书");
        
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(bookRepository.findByBarcode("123456")).thenReturn(book);
        when(borrowRecordRepository.getUnreturnedCountByReader(1)).thenReturn(2);
        doNothing().when(bookRepository).updateStatus(eq(1), eq("借出"), any(Connection.class));
        when(borrowRecordRepository.add(any(BorrowRecord.class), any(Connection.class))).thenReturn(1);
        doNothing().when(connection).commit();
        
        BorrowingService.BorrowResult result = service.borrowBook("1", "123456");
        
        assertTrue(result.success);
        assertEquals("图书借阅成功", result.message);
        assertEquals("成功借阅《测试图书》", result.data);
        
        verify(bookRepository).updateStatus(eq(1), eq("借出"), any(Connection.class));
        verify(borrowRecordRepository).add(any(BorrowRecord.class), any(Connection.class));
        verify(connection).commit();
    }

    @Test
    void returnBook_shouldReturnFailureWhenParametersAreNull() {
        BorrowingService.ReturnResult result = service.returnBook(null, null);
        assertFalse(result.success);
        assertEquals("读者ID和图书条形码不能为空", result.message);
    }

    @Test
    void returnBook_shouldReturnFailureWhenReaderIdIsNotNumeric() {
        BorrowingService.ReturnResult result = service.returnBook("abc", "123456");
        assertFalse(result.success);
        assertEquals("读者ID必须是数字", result.message);
    }

    @Test
    void returnBook_shouldReturnFailureWhenBookNotFound() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(bookRepository.findByBarcode("123456")).thenReturn(null);
        
        BorrowingService.ReturnResult result = service.returnBook("1", "123456");
        
        assertFalse(result.success);
        assertEquals("未找到该条形码对应的图书", result.message);
        verify(connection).rollback();
    }

    @Test
    void getUnreturnedCountByReader_shouldReturnZeroWhenReaderIdIsInvalid() {
        int count = service.getUnreturnedCountByReader("abc");
        assertEquals(0, count);
    }

    @Test
    void getUnreturnedCountByReader_shouldReturnRepositoryResult() {
        when(borrowRecordRepository.getUnreturnedCountByReader(1)).thenReturn(3);
        int count = service.getUnreturnedCountByReader("1");
        assertEquals(3, count);
        verify(borrowRecordRepository).getUnreturnedCountByReader(1);
    }

    @Test
    void getOverdueUnreturnedCountByReader_shouldReturnZeroWhenReaderIdIsInvalid() {
        int count = service.getOverdueUnreturnedCountByReader("abc");
        assertEquals(0, count);
    }

    @Test
    void getOverdueUnreturnedCountByReader_shouldReturnRepositoryResult() {
        when(borrowRecordRepository.getOverdueUnreturnedCountByReader(1)).thenReturn(2);
        int count = service.getOverdueUnreturnedCountByReader("1");
        assertEquals(2, count);
        verify(borrowRecordRepository).getOverdueUnreturnedCountByReader(1);
    }

    /**
     * 使用 Unsafe 分配实例，避免执行构造函数
     */
    private BorrowingService allocateWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BorrowingService) allocateInstance.invoke(unsafe, BorrowingService.class);
    }
}