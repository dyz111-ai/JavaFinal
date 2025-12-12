package com.example.demo0.book.repository;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.model.PhysicalBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookRepository 单元测试
 * 使用Mock隔离数据库依赖
 */
class BookRepositoryUnitTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private BookRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        
        // 使用 Unsafe 绕过构造函数中的 JNDI 查找
        repository = allocateWithoutConstructor();
        
        // 注入 mock 的 DataSource
        Field dataSourceField = BookRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(repository, dataSource);
    }
    
    /**
     * 使用 Unsafe 分配实例，避免执行构造函数中的 JNDI 查找。
     */
    private BookRepository allocateWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BookRepository) allocateInstance.invoke(unsafe, BookRepository.class);
    }

    @Test
    void search_shouldReturnEmptyListWhenKeywordIsNull() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        var result = repository.search(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void search_shouldReturnEmptyListWhenKeywordIsEmpty() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        var result = repository.search("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    void search_shouldReturnBooksWhenKeywordProvided() throws SQLException {
        when(connection.prepareStatement(contains("WHERE"))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getString("isbn")).thenReturn("9781234567890");
        when(resultSet.getString("title")).thenReturn("测试图书");
        when(resultSet.getString("author")).thenReturn("测试作者");
        when(resultSet.getInt("stock")).thenReturn(10);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getString("categories")).thenReturn("小说, 文学");

        var result = repository.search("测试");

        assertEquals(1, result.size());
        assertEquals("9781234567890", result.get(0).getISBN());
        assertEquals("测试图书", result.get(0).getTitle());
        assertEquals("测试作者", result.get(0).getAuthor());
        assertEquals(10, result.get(0).getStock());
        assertTrue(result.get(0).getCategories().contains("小说"));
        assertTrue(result.get(0).getCategories().contains("文学"));

        // 验证 SQL 参数设置
        verify(preparedStatement, times(3)).setString(anyInt(), eq("%测试%"));
    }

    @Test
    void search_shouldHandleNullCategories() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getString("isbn")).thenReturn("9781234567890");
        when(resultSet.getString("title")).thenReturn("测试图书");
        when(resultSet.getString("author")).thenReturn("测试作者");
        when(resultSet.getInt("stock")).thenReturn(5);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getString("categories")).thenReturn(null);

        var result = repository.search("测试");

        assertEquals(1, result.size());
        assertTrue(result.get(0).getCategories().isEmpty());
    }

    @Test
    void search_shouldHandleEmptyKeywordAndReturnAllBooks() throws SQLException {
        when(connection.prepareStatement(argThat(sql -> !sql.contains("WHERE")))).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        when(resultSet.getString("isbn")).thenReturn("9781111111111").thenReturn("9782222222222");
        when(resultSet.getString("title")).thenReturn("图书1").thenReturn("图书2");
        when(resultSet.getString("author")).thenReturn("作者1").thenReturn("作者2");
        when(resultSet.getInt("stock")).thenReturn(1).thenReturn(2);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getString("categories")).thenReturn("分类1").thenReturn("分类2");

        var result = repository.search("");

        assertEquals(2, result.size());
        verify(preparedStatement, never()).setString(anyInt(), anyString());
    }

    @Test
    void findByIsbn_shouldReturnNullWhenIsbnIsNull() {
        assertNull(repository.findByIsbn(null));
    }

    @Test
    void findByIsbn_shouldReturnNullWhenIsbnIsEmpty() {
        assertNull(repository.findByIsbn("   "));
    }

    @Test
    void findByIsbn_shouldReturnBookWhenFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getString("isbn")).thenReturn("9781234567890");
        when(resultSet.getString("title")).thenReturn("测试图书");
        when(resultSet.getString("author")).thenReturn("测试作者");
        when(resultSet.getInt("stock")).thenReturn(10);
        when(resultSet.wasNull()).thenReturn(false);

        BookInfo result = repository.findByIsbn("9781234567890");

        assertNotNull(result);
        assertEquals("9781234567890", result.getISBN());
        assertEquals("测试图书", result.getTitle());
        assertEquals("测试作者", result.getAuthor());
        assertEquals(10, result.getStock());
        verify(preparedStatement).setString(1, "9781234567890");
    }

    @Test
    void findByIsbn_shouldReturnNullWhenNotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        BookInfo result = repository.findByIsbn("9789999999999");

        assertNull(result);
    }

    @Test
    void findPhysicalBooksByKeyword_shouldReturnEmptyListWhenKeywordIsNull() {
        var result = repository.findPhysicalBooksByKeyword(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void findPhysicalBooksByKeyword_shouldReturnEmptyListWhenKeywordIsEmpty() {
        var result = repository.findPhysicalBooksByKeyword("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void findPhysicalBooksByKeyword_shouldReturnPhysicalBooks() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getObject("bookid")).thenReturn(1);
        when(resultSet.getString("barcode")).thenReturn("BAR001");
        when(resultSet.getString("status")).thenReturn("正常");
        when(resultSet.getObject("shelfid")).thenReturn(10);
        when(resultSet.getString("isbn")).thenReturn("9781234567890");
        when(resultSet.getString("title")).thenReturn("测试图书");
        when(resultSet.getObject("buildingid")).thenReturn(1);
        when(resultSet.getString("buildingname")).thenReturn("主楼");
        when(resultSet.getString("shelfcode")).thenReturn("A01");
        when(resultSet.getObject("floor")).thenReturn(1);
        when(resultSet.getString("zone")).thenReturn("A区");

        var result = repository.findPhysicalBooksByKeyword("测试");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBookId());
        assertEquals("BAR001", result.get(0).getBarcode());
        assertEquals("正常", result.get(0).getStatus());
        assertEquals("9781234567890", result.get(0).getIsbn());
        assertEquals("测试图书", result.get(0).getTitle());
        assertEquals("主楼", result.get(0).getBuildingName());
        verify(preparedStatement).setString(1, "%测试%");
    }

    @Test
    void findPhysicalBooksByIsbn_shouldReturnEmptyListWhenIsbnIsNull() {
        var result = repository.findPhysicalBooksByIsbn(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void findPhysicalBooksByIsbn_shouldReturnPhysicalBooks() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        when(resultSet.getObject("bookid")).thenReturn(1);
        when(resultSet.getString("barcode")).thenReturn("BAR001");
        when(resultSet.getString("status")).thenReturn("正常");
        when(resultSet.getObject("shelfid")).thenReturn(10);
        when(resultSet.getString("isbn")).thenReturn("9781234567890");
        when(resultSet.getString("title")).thenReturn("测试图书");
        when(resultSet.getObject("buildingid")).thenReturn(1);
        when(resultSet.getString("buildingname")).thenReturn("主楼");
        when(resultSet.getString("shelfcode")).thenReturn("A01");
        when(resultSet.getObject("floor")).thenReturn(1);
        when(resultSet.getString("zone")).thenReturn("A区");

        var result = repository.findPhysicalBooksByIsbn("9781234567890");

        assertEquals(1, result.size());
        assertEquals("9781234567890", result.get(0).getIsbn());
        verify(preparedStatement).setString(1, "9781234567890");
    }

    @Test
    void findByBarcode_shouldReturnNullWhenNotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        PhysicalBook result = repository.findByBarcode("INVALID");

        assertNull(result);
    }

    @Test
    void findByBarcode_shouldReturnPhysicalBookWhenFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getInt("bookid")).thenReturn(1);
        when(resultSet.getString("barcode")).thenReturn("BAR001");
        when(resultSet.getString("status")).thenReturn("正常");
        when(resultSet.getObject("shelfid")).thenReturn(10);
        when(resultSet.getString("isbn")).thenReturn("9781234567890");
        when(resultSet.getString("title")).thenReturn("测试图书");

        PhysicalBook result = repository.findByBarcode("BAR001");

        assertNotNull(result);
        assertEquals(1, result.getBookId());
        assertEquals("BAR001", result.getBarcode());
        assertEquals("9781234567890", result.getIsbn());
        verify(preparedStatement).setString(1, "BAR001");
    }

    @Test
    void updateStatus_shouldUpdateBookStatus() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        int result = repository.updateStatus(1, "借出", connection);

        assertEquals(1, result);
        verify(preparedStatement).setString(1, "借出");
        verify(preparedStatement).setInt(2, 1);
    }

    @Test
    void updateStatus_shouldReturnZeroWhenNoRowsUpdated() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        int result = repository.updateStatus(999, "借出", connection);

        assertEquals(0, result);
    }
}

