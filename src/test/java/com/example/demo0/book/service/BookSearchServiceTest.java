package com.example.demo0.book.service;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * BookSearchService 单元测试
 * 使用Mock隔离仓储层依赖
 */
class BookSearchServiceTest {

    private BookSearchService service;

    @Mock
    private BookRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用反射绕过构造函数中的 Repository 实例化
        service = allocateWithoutConstructor();
        Field repoField = BookSearchService.class.getDeclaredField("repository");
        repoField.setAccessible(true);
        repoField.set(service, repository);
    }

    @Test
    void search_shouldCallRepositoryWithKeyword() {
        List<BookInfo> mockBooks = createMockBooks();
        when(repository.search("测试")).thenReturn(mockBooks);

        List<BookInfo> result = service.search("测试");

        assertEquals(1, result.size());
        verify(repository).search("测试");
    }

    @Test
    void search_shouldReturnEmptyListWhenRepositoryReturnsEmpty() {
        when(repository.search("不存在")).thenReturn(Collections.emptyList());

        List<BookInfo> result = service.search("不存在");

        assertTrue(result.isEmpty());
        verify(repository).search("不存在");
    }

    @Test
    void searchAndFilter_shouldReturnAllBooksWhenCategoryIsNull() {
        List<BookInfo> mockBooks = createMockBooks();
        when(repository.search("测试")).thenReturn(mockBooks);

        List<BookInfo> result = service.searchAndFilter("测试", null);

        assertEquals(1, result.size());
        verify(repository).search("测试");
    }

    @Test
    void searchAndFilter_shouldReturnAllBooksWhenCategoryIsBlank() {
        List<BookInfo> mockBooks = createMockBooks();
        when(repository.search("测试")).thenReturn(mockBooks);

        List<BookInfo> result = service.searchAndFilter("测试", "   ");

        assertEquals(1, result.size());
        verify(repository).search("测试");
    }

    @Test
    void searchAndFilter_shouldReturnAllBooksWhenCategoryIsAll() {
        List<BookInfo> mockBooks = createMockBooks();
        when(repository.search("测试")).thenReturn(mockBooks);

        List<BookInfo> result = service.searchAndFilter("测试", "全部");

        assertEquals(1, result.size());
        verify(repository).search("测试");
    }

    @Test
    void searchAndFilter_shouldFilterByCategory() {
        List<BookInfo> books = new ArrayList<>();
        
        BookInfo book1 = new BookInfo();
        book1.setISBN("9781111111111");
        book1.setTitle("小说1");
        book1.setCategories(Arrays.asList("小说", "文学"));
        books.add(book1);
        
        BookInfo book2 = new BookInfo();
        book2.setISBN("9782222222222");
        book2.setTitle("科技1");
        book2.setCategories(Arrays.asList("科技", "计算机"));
        books.add(book2);
        
        BookInfo book3 = new BookInfo();
        book3.setISBN("9783333333333");
        book3.setTitle("小说2");
        book3.setCategories(Arrays.asList("小说", "历史"));
        books.add(book3);

        when(repository.search("测试")).thenReturn(books);

        List<BookInfo> result = service.searchAndFilter("测试", "小说");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getCategories().contains("小说")));
        verify(repository).search("测试");
    }

    @Test
    void searchAndFilter_shouldReturnEmptyListWhenNoMatch() {
        List<BookInfo> books = new ArrayList<>();
        
        BookInfo book1 = new BookInfo();
        book1.setISBN("9781111111111");
        book1.setTitle("科技1");
        book1.setCategories(Arrays.asList("科技", "计算机"));
        books.add(book1);

        when(repository.search("测试")).thenReturn(books);

        List<BookInfo> result = service.searchAndFilter("测试", "小说");

        assertTrue(result.isEmpty());
        verify(repository).search("测试");
    }

    @Test
    void extractCategories_shouldReturnEmptyListWhenBooksIsNull() {
        List<String> result = service.extractCategories(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void extractCategories_shouldReturnEmptyListWhenBooksIsEmpty() {
        List<String> result = service.extractCategories(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void extractCategories_shouldExtractUniqueCategories() {
        List<BookInfo> books = new ArrayList<>();
        
        BookInfo book1 = new BookInfo();
        book1.setCategories(Arrays.asList("小说", "文学"));
        books.add(book1);
        
        BookInfo book2 = new BookInfo();
        book2.setCategories(Arrays.asList("小说", "历史"));
        books.add(book2);
        
        BookInfo book3 = new BookInfo();
        book3.setCategories(Arrays.asList("科技"));
        books.add(book3);

        List<String> result = service.extractCategories(books);

        assertEquals(4, result.size()); // 小说, 文学, 历史, 科技
        assertTrue(result.contains("小说"));
        assertTrue(result.contains("文学"));
        assertTrue(result.contains("历史"));
        assertTrue(result.contains("科技"));
    }

    @Test
    void extractCategories_shouldHandleNullCategories() {
        List<BookInfo> books = new ArrayList<>();
        
        BookInfo book1 = new BookInfo();
        book1.setCategories(null);
        books.add(book1);
        
        BookInfo book2 = new BookInfo();
        book2.setCategories(Arrays.asList("小说"));
        books.add(book2);

        List<String> result = service.extractCategories(books);

        assertEquals(1, result.size());
        assertTrue(result.contains("小说"));
    }

    @Test
    void extractCategories_shouldMaintainOrder() {
        List<BookInfo> books = new ArrayList<>();
        
        BookInfo book1 = new BookInfo();
        book1.setCategories(Arrays.asList("A", "B"));
        books.add(book1);
        
        BookInfo book2 = new BookInfo();
        book2.setCategories(Arrays.asList("C", "A"));
        books.add(book2);

        List<String> result = service.extractCategories(books);

        // 应该保持插入顺序（LinkedHashSet）
        assertEquals(3, result.size());
        assertEquals("A", result.get(0));
        assertEquals("B", result.get(1));
        assertEquals("C", result.get(2));
    }

    private List<BookInfo> createMockBooks() {
        BookInfo book = new BookInfo();
        book.setISBN("9781234567890");
        book.setTitle("测试图书");
        book.setAuthor("测试作者");
        book.setCategories(Arrays.asList("小说", "文学"));
        return Arrays.asList(book);
    }

    /**
     * 使用 Unsafe 分配实例，避免执行构造函数中的 Repository 实例化。
     */
    private BookSearchService allocateWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BookSearchService) allocateInstance.invoke(unsafe, BookSearchService.class);
    }
}

