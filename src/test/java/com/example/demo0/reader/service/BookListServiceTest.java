package com.example.demo0.reader.service;

import com.example.demo0.reader.model.BookList;
import com.example.demo0.reader.repository.BookListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookListService 单元测试
 * 使用Mock隔离仓储层依赖
 */
class BookListServiceTest {

    private BookListService service;

    @Mock
    private BookListRepository bookListRepository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用反射绕过构造函数
        service = allocateWithoutConstructor();
        
        // 设置模拟的依赖
        Field repoField = BookListService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, bookListRepository);
    }

    @Test
    void createBookList_shouldCreateBookListSuccessfully() throws SQLException {
        // 准备测试数据
        String name = "测试书单";
        String introduction = "这是一个测试书单";
        Integer creatorId = 1;
        
        // 模拟repository返回的BookList
        BookList expected = new BookList();
        expected.setBooklistId(1);
        expected.setBooklistName(name);
        expected.setBooklistIntroduction(introduction);
        expected.setCreatorId(creatorId);
        expected.setListCode("test1234");
        
        // 模拟repository方法
        when(bookListRepository.createBookList(any(BookList.class))).thenReturn(expected);
        
        // 执行测试
        BookList actual = service.createBookList(name, introduction, creatorId);
        
        // 验证结果
        assertNotNull(actual);
        assertEquals(expected.getBooklistId(), actual.getBooklistId());
        assertEquals(expected.getBooklistName(), actual.getBooklistName());
        assertEquals(expected.getBooklistIntroduction(), actual.getBooklistIntroduction());
        assertEquals(expected.getCreatorId(), actual.getCreatorId());
        assertNotNull(actual.getListCode());
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).createBookList(any(BookList.class));
    }

    @Test
    void searchBooklistsByReader_shouldReturnCreatedAndCollectedBooklists() throws SQLException {
        // 准备测试数据
        Integer readerId = 1;
        
        // 模拟创建的书单
        List<BookList> created = new ArrayList<>();
        BookList createdBookList = new BookList();
        createdBookList.setBooklistId(1);
        createdBookList.setBooklistName("创建的书单");
        created.add(createdBookList);
        
        // 模拟收藏的书单
        List<BookList> collected = new ArrayList<>();
        BookList collectedBookList = new BookList();
        collectedBookList.setBooklistId(2);
        collectedBookList.setBooklistName("收藏的书单");
        collected.add(collectedBookList);
        
        // 模拟repository方法
        when(bookListRepository.findCreatedBooklistsByReaderId(readerId)).thenReturn(created);
        when(bookListRepository.findCollectedBooklistsByReaderId(readerId)).thenReturn(collected);
        
        // 执行测试
        Map<String, List<BookList>> result = service.searchBooklistsByReader(readerId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Created"));
        assertTrue(result.containsKey("Collected"));
        assertEquals(1, result.get("Created").size());
        assertEquals(1, result.get("Collected").size());
        assertEquals("创建的书单", result.get("Created").get(0).getBooklistName());
        assertEquals("收藏的书单", result.get("Collected").get(0).getBooklistName());
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).findCreatedBooklistsByReaderId(readerId);
        verify(bookListRepository, times(1)).findCollectedBooklistsByReaderId(readerId);
    }

    @Test
    void getBookListDetails_shouldReturnBookList() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        
        // 模拟repository返回的BookList
        BookList expected = new BookList();
        expected.setBooklistId(booklistId);
        expected.setBooklistName("测试书单");
        
        // 模拟repository方法
        when(bookListRepository.findBookListById(booklistId)).thenReturn(expected);
        
        // 执行测试
        BookList actual = service.getBookListDetails(booklistId);
        
        // 验证结果
        assertNotNull(actual);
        assertEquals(expected.getBooklistId(), actual.getBooklistId());
        assertEquals(expected.getBooklistName(), actual.getBooklistName());
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).findBookListById(booklistId);
    }

    @Test
    void deleteBookList_shouldReturnFalseWhenBookListNotOwnedByReader() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        Integer readerId = 1;
        
        // 模拟repository方法
        when(bookListRepository.checkBooklistOwnership(booklistId, readerId)).thenReturn(false);
        
        // 执行测试
        boolean result = service.deleteBookList(booklistId, readerId);
        
        // 验证结果
        assertFalse(result);
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).checkBooklistOwnership(booklistId, readerId);
        verify(bookListRepository, times(0)).deleteBookList(booklistId);
    }

    @Test
    void deleteBookList_shouldReturnTrueWhenBookListDeletedSuccessfully() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        Integer readerId = 1;
        
        // 模拟repository方法
        when(bookListRepository.checkBooklistOwnership(booklistId, readerId)).thenReturn(true);
        when(bookListRepository.deleteBookList(booklistId)).thenReturn(1);
        
        // 执行测试
        boolean result = service.deleteBookList(booklistId, readerId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).checkBooklistOwnership(booklistId, readerId);
        verify(bookListRepository, times(1)).deleteBookList(booklistId);
    }

    @Test
    void collectBooklist_shouldReturnTrueWhenBookListCollectedSuccessfully() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        Integer readerId = 1;
        String notes = "收藏备注";
        
        // 模拟repository方法
        when(bookListRepository.collectBooklist(booklistId, readerId, notes)).thenReturn(1);
        
        // 执行测试
        boolean result = service.collectBooklist(booklistId, readerId, notes);
        
        // 验证结果
        assertTrue(result);
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).collectBooklist(booklistId, readerId, notes);
    }

    @Test
    void cancelCollectBooklist_shouldReturnTrueWhenBookListCollectionCancelledSuccessfully() throws SQLException {
        // 准备测试数据
        Integer booklistId = 1;
        Integer readerId = 1;
        
        // 模拟repository方法
        when(bookListRepository.cancelCollectBooklist(booklistId, readerId)).thenReturn(1);
        
        // 执行测试
        boolean result = service.cancelCollectBooklist(booklistId, readerId);
        
        // 验证结果
        assertTrue(result);
        
        // 验证repository方法被调用
        verify(bookListRepository, times(1)).cancelCollectBooklist(booklistId, readerId);
    }

    // 使用Unsafe绕过构造函数
    private <T> T allocateWithoutConstructor() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
        return (T) unsafe.allocateInstance(BookListService.class);
    }
}
