package com.example.demo0.reader.repository;

import com.example.demo0.reader.model.BorrowRecordDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BorrowRecordRepository 单元测试
 * 使用Mock隔离数据库依赖
 */
class BorrowRecordRepositoryUnitTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private BorrowRecordRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        
        // 使用 Unsafe 绕过构造函数中的 JNDI 查找
        repository = allocateWithoutConstructor();
        
        // 注入 mock 的 DataSource
        Field dataSourceField = BorrowRecordRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(repository, dataSource);
    }
    
    /**
     * 使用 Unsafe 分配实例，避免执行构造函数中的 JNDI 查找。
     */
    private BorrowRecordRepository allocateWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BorrowRecordRepository) allocateInstance.invoke(unsafe, BorrowRecordRepository.class);
    }

    @Test
    void getUnreturnedCountByReader_shouldReturnCount() throws SQLException {
        int readerId = 1;
        int expectedCount = 3;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(expectedCount);
        
        int actualCount = repository.getUnreturnedCountByReader(readerId);
        
        assertEquals(expectedCount, actualCount);
        verify(preparedStatement).setInt(1, readerId);
    }

    @Test
    void getUnreturnedCountByReader_shouldReturnZeroWhenNoResults() throws SQLException {
        int readerId = 1;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        int actualCount = repository.getUnreturnedCountByReader(readerId);
        
        assertEquals(0, actualCount);
    }

    @Test
    void getOverdueUnreturnedCountByReader_shouldReturnCount() throws SQLException {
        int readerId = 1;
        int expectedCount = 2;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(expectedCount);
        
        int actualCount = repository.getOverdueUnreturnedCountByReader(readerId);
        
        assertEquals(expectedCount, actualCount);
        verify(preparedStatement).setInt(1, readerId);
    }

    @Test
    void getOverdueUnreturnedCountByReader_shouldReturnZeroWhenNoResults() throws SQLException {
        int readerId = 1;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        int actualCount = repository.getOverdueUnreturnedCountByReader(readerId);
        
        assertEquals(0, actualCount);
    }

    @Test
    void hasAnyRecord_shouldReturnTrueWhenRecordExists() throws SQLException {
        int readerId = 1;
        int bookId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        boolean result = repository.hasAnyRecord(readerId, bookId, connection);
        
        assertTrue(result);
        verify(preparedStatement).setInt(1, readerId);
        verify(preparedStatement).setInt(2, bookId);
    }

    @Test
    void hasAnyRecord_shouldReturnFalseWhenRecordDoesNotExist() throws SQLException {
        int readerId = 1;
        int bookId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        boolean result = repository.hasAnyRecord(readerId, bookId, connection);
        
        assertFalse(result);
    }

    @Test
    void findById_shouldReturnBorrowRecordDetail() throws SQLException {
        int borrowRecordId = 1;
        Timestamp borrowTime = Timestamp.from(Instant.now());
        Timestamp returnTime = Timestamp.from(Instant.now().plusSeconds(86400)); // 一天后
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getObject("BorrowRecordID")).thenReturn(borrowRecordId);
        when(resultSet.getInt("BorrowRecordID")).thenReturn(borrowRecordId);
        when(resultSet.getInt("BookID")).thenReturn(10);
        when(resultSet.getString("ISBN")).thenReturn("9781234567890");
        when(resultSet.getString("BookTitle")).thenReturn("测试图书");
        when(resultSet.getString("BookAuthor")).thenReturn("测试作者");
        when(resultSet.getInt("ReaderID")).thenReturn(100);
        when(resultSet.getString("ReaderName")).thenReturn("测试读者");
        when(resultSet.getTimestamp("BorrowTime")).thenReturn(borrowTime);
        when(resultSet.getTimestamp("ReturnTime")).thenReturn(returnTime);
        when(resultSet.getBigDecimal("OverdueFine")).thenReturn(new BigDecimal("2.50"));
        
        BorrowRecordDetail result = repository.findById(borrowRecordId);
        
        assertNotNull(result);
        assertEquals(borrowRecordId, result.getBorrowRecordId());
        assertEquals("10", result.getBookId());
        assertEquals("9781234567890", result.getIsbn());
        assertEquals("测试图书", result.getBookTitle());
        assertEquals("100", result.getReaderId());
        assertEquals("测试读者", result.getReaderName());
        assertEquals(borrowTime, result.getBorrowTime());
        assertEquals(returnTime, result.getReturnTime());
        assertEquals(new BigDecimal("2.50"), result.getOverdueFine());
        
        verify(preparedStatement).setInt(1, borrowRecordId);
    }

    @Test
    void findById_shouldReturnNullWhenNotFound() throws SQLException {
        int borrowRecordId = 999;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        BorrowRecordDetail result = repository.findById(borrowRecordId);
        
        assertNull(result);
    }

    @Test
    void findByReaderId_shouldReturnEmptyListWhenNoRecords() throws SQLException {
        int readerId = 1;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        
        List<BorrowRecordDetail> result = repository.findByReaderId(readerId);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(preparedStatement).setInt(1, readerId);
    }

    @Test
    void findByReaderId_shouldReturnMultipleRecords() throws SQLException {
        int readerId = 1;
        Timestamp borrowTime = Timestamp.from(Instant.now());
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        // 模拟两行结果
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        
        // 第一行数据
        when(resultSet.getObject("BorrowRecordID")).thenReturn(1).thenReturn(2);
        when(resultSet.getInt("BookID")).thenReturn(10).thenReturn(20);
        when(resultSet.getString("ISBN")).thenReturn("9781234567890").thenReturn("9780987654321");
        when(resultSet.getString("BookTitle")).thenReturn("图书1").thenReturn("图书2");
        when(resultSet.getString("BookAuthor")).thenReturn("作者1").thenReturn("作者2");
        when(resultSet.getInt("ReaderID")).thenReturn(1).thenReturn(1);
        when(resultSet.getString("ReaderName")).thenReturn("测试读者").thenReturn("测试读者");
        when(resultSet.getTimestamp("BorrowTime")).thenReturn(borrowTime).thenReturn(borrowTime);
        when(resultSet.getTimestamp("ReturnTime")).thenReturn(null).thenReturn(null);
        when(resultSet.getBigDecimal("OverdueFine")).thenReturn(BigDecimal.ZERO).thenReturn(BigDecimal.ZERO);
        when(resultSet.getInt("BorrowRecordID")).thenReturn(1).thenReturn(2);
        
        List<BorrowRecordDetail> result = repository.findByReaderId(readerId);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getBorrowRecordId());
        assertEquals("9781234567890", result.get(0).getIsbn());
        assertEquals(2, result.get(1).getBorrowRecordId());
        assertEquals("9780987654321", result.get(1).getIsbn());
        
        verify(preparedStatement).setInt(1, readerId);
    }
}
