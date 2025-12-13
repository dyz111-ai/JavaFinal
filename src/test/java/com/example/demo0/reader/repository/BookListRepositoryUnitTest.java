package com.example.demo0.reader.repository;

import com.example.demo0.reader.model.BookList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookListRepository 单元测试
 * 使用Mock隔离数据源和数据库依赖
 */
class BookListRepositoryUnitTest {

    private BookListRepository repository;

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private ResultSet generatedKeys;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用反射绕过构造函数
        repository = allocateWithoutConstructor();
        
        // 设置模拟的依赖
        Field dataSourceField = BookListRepository.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        dataSourceField.set(repository, dataSource);
    }

    @Test
    void checkBooklistOwnership_shouldReturnTrueWhenBooklistBelongsToReader() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        Integer readerId = 1;
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // 表示找到一条记录
        
        // 执行测试
        boolean result = repository.checkBooklistOwnership(booklistId, readerId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(anyString());
        verify(preparedStatement, times(1)).setInt(1, booklistId);
        verify(preparedStatement, times(1)).setInt(2, readerId);
        verify(preparedStatement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        verify(resultSet, times(1)).getInt(1);
    }

    @Test
    void checkBooklistOwnership_shouldReturnFalseWhenBooklistDoesNotBelongToReader() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        Integer readerId = 1;
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0); // 表示没有找到记录
        
        // 执行测试
        boolean result = repository.checkBooklistOwnership(booklistId, readerId);
        
        // 验证结果
        assertFalse(result);
    }

    @Test
    void findBookListById_shouldReturnBookListWhenFound() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        String listCode = "test1234";
        String booklistName = "测试书单";
        String booklistIntroduction = "这是一个测试书单";
        Integer creatorId = 1;
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("booklistid")).thenReturn(booklistId);
        when(resultSet.getString("listcode")).thenReturn(listCode);
        when(resultSet.getString("booklistname")).thenReturn(booklistName);
        // 模拟CLOB读取失败，然后字符串读取成功
        when(resultSet.getClob("booklistintroduction")).thenThrow(new SQLException("Not a CLOB"));
        when(resultSet.getString("booklistintroduction")).thenReturn(booklistIntroduction);
        when(resultSet.getInt("creatorid")).thenReturn(creatorId);
        
        // 执行测试
        BookList result = repository.findBookListById(booklistId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(booklistId, result.getBooklistId());
        assertEquals(listCode, result.getListCode());
        assertEquals(booklistName, result.getBooklistName());
        assertEquals(booklistIntroduction, result.getBooklistIntroduction());
        assertEquals(creatorId, result.getCreatorId());
    }

    @Test
    void findBookListById_shouldReturnNullWhenBookListNotFound() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // 表示没有找到记录
        
        // 执行测试
        BookList result = repository.findBookListById(booklistId);
        
        // 验证结果
        assertNull(result);
    }

    @Test
    void createBookList_shouldCreateBookListSuccessfully() throws SQLException {
        // 准备测试数据
        BookList booklist = new BookList();
        booklist.setBooklistName("测试书单");
        booklist.setBooklistIntroduction("这是一个测试书单");
        booklist.setCreatorId(1);
        booklist.setListCode("test1234");
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(1); // 生成的booklistid
        
        // 执行测试
        BookList result = repository.createBookList(booklist);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getBooklistId());
        assertEquals("测试书单", result.getBooklistName());
        assertEquals("这是一个测试书单", result.getBooklistIntroduction());
        assertEquals(1, result.getCreatorId());
        assertEquals("test1234", result.getListCode());
        
        // 验证方法调用
        verify(preparedStatement, times(1)).setString(1, "test1234");
        verify(preparedStatement, times(1)).setString(2, "测试书单");
        verify(preparedStatement, times(1)).setString(3, "这是一个测试书单");
        verify(preparedStatement, times(1)).setInt(4, 1);
    }

    @Test
    void findCreatedBooklistsByReaderId_shouldReturnBooklists() throws SQLException {
        // 准备测试数据
        Integer readerId = 1;
        String listCode = "test1234";
        String booklistName = "测试书单";
        String booklistIntroduction = "这是一个测试书单";
        Integer creatorId = 1;
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false); // 返回一条记录
        when(resultSet.getInt("booklistid")).thenReturn(1);
        when(resultSet.getString("listcode")).thenReturn(listCode);
        when(resultSet.getString("booklistname")).thenReturn(booklistName);
        // 模拟CLOB读取失败，然后字符串读取成功
        when(resultSet.getClob("booklistintroduction")).thenThrow(new SQLException("Not a CLOB"));
        when(resultSet.getString("booklistintroduction")).thenReturn(booklistIntroduction);
        when(resultSet.getInt("creatorid")).thenReturn(creatorId);
        
        // 执行测试
        List<BookList> result = repository.findCreatedBooklistsByReaderId(readerId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getBooklistId());
        assertEquals(listCode, result.get(0).getListCode());
        assertEquals(booklistName, result.get(0).getBooklistName());
        assertEquals(booklistIntroduction, result.get(0).getBooklistIntroduction());
        assertEquals(creatorId, result.get(0).getCreatorId());
    }

    @Test
    void deleteBookList_shouldDeleteBookListSuccessfully() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        
        // 模拟数据源和连接
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        
        // 执行测试
        int result = repository.deleteBookList(booklistId);
        
        // 验证结果
        assertEquals(1, result);
        
        // 验证方法调用
        verify(connection, times(1)).setAutoCommit(false);
        // 验证删除关联表
        verify(connection, times(3)).prepareStatement(anyString());
        verify(preparedStatement, atLeast(3)).setInt(1, booklistId);
        verify(preparedStatement, atLeast(3)).executeUpdate();
        verify(connection, times(1)).commit();
    }

    // 使用Unsafe绕过构造函数
    private <T> T allocateWithoutConstructor() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
        return (T) unsafe.allocateInstance(BookListRepository.class);
    }
}
