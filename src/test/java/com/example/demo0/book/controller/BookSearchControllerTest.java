package com.example.demo0.book.controller;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.service.BookSearchService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * BookSearchController 单元测试
 * 使用Mock隔离Service和Servlet依赖
 */
class BookSearchControllerTest {

    private BookSearchController controller;

    @Mock
    private BookSearchService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        controller = allocateWithoutConstructor();
        Field serviceField = BookSearchController.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(controller, service);
    }

    @Test
    void doGet_shouldSearchWithKeywordParameter() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn("测试");
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> mockBooks = createMockBooks();
        when(service.search("测试")).thenReturn(mockBooks);
        when(service.extractCategories(mockBooks)).thenReturn(Arrays.asList("小说", "文学"));

        controller.doGet(request, response);

        verify(service, times(2)).search("测试"); // 调用两次：一次用于搜索，一次用于提取分类
        verify(service).extractCategories(mockBooks);
        verify(request).setAttribute("keyword", "测试");
        verify(request).setAttribute("selectedCategory", "全部");
        verify(request).setAttribute("books", mockBooks);
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_shouldUseQParameterWhenKeywordIsNull() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn(null);
        when(request.getParameter("q")).thenReturn("查询");
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> mockBooks = createMockBooks();
        when(service.search("查询")).thenReturn(mockBooks);
        when(service.extractCategories(mockBooks)).thenReturn(Collections.emptyList());

        controller.doGet(request, response);

        verify(service, times(2)).search("查询"); // 调用两次：一次用于搜索，一次用于提取分类
        verify(request).setAttribute("keyword", "查询");
    }

    @Test
    void doGet_shouldUseQParameterWhenKeywordIsBlank() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn("   ");
        when(request.getParameter("q")).thenReturn("备用查询");
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> mockBooks = createMockBooks();
        when(service.search("备用查询")).thenReturn(mockBooks);
        when(service.extractCategories(mockBooks)).thenReturn(Collections.emptyList());

        controller.doGet(request, response);

        verify(service, times(2)).search("备用查询"); // 调用两次：一次用于搜索，一次用于提取分类
        verify(request).setAttribute("keyword", "备用查询");
    }

    @Test
    void doGet_shouldFilterByCategory() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn("测试");
        when(request.getParameter("category")).thenReturn("小说");
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> allBooks = createMockBooks();
        List<BookInfo> filteredBooks = createMockBooks();
        
        when(service.search("测试")).thenReturn(allBooks);
        when(service.searchAndFilter("测试", "小说")).thenReturn(filteredBooks);
        when(service.extractCategories(allBooks)).thenReturn(Arrays.asList("小说", "文学"));

        controller.doGet(request, response);

        verify(service).searchAndFilter("测试", "小说");
        verify(service, times(1)).search("测试"); // 用于提取分类（只调用一次，因为搜索使用了searchAndFilter）
        verify(request).setAttribute("selectedCategory", "小说");
        verify(request).setAttribute("books", filteredBooks);
    }

    @Test
    void doGet_shouldHandleAllCategory() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn("测试");
        when(request.getParameter("category")).thenReturn("全部");
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> mockBooks = createMockBooks();
        when(service.search("测试")).thenReturn(mockBooks);
        when(service.extractCategories(mockBooks)).thenReturn(Arrays.asList("小说"));

        controller.doGet(request, response);

        verify(service, times(2)).search("测试"); // 调用两次：一次用于搜索，一次用于提取分类
        verify(service, never()).searchAndFilter(anyString(), anyString());
        verify(request).setAttribute("selectedCategory", "全部");
    }

    @Test
    void doGet_shouldHandleEmptyKeyword() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn(null);
        when(request.getParameter("q")).thenReturn(null);
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        when(service.search(null)).thenReturn(Collections.emptyList());
        when(service.extractCategories(Collections.emptyList())).thenReturn(Collections.emptyList());

        controller.doGet(request, response);

        verify(service, times(2)).search(null); // 调用两次：一次用于搜索，一次用于提取分类
        verify(request).setAttribute("keyword", "");
        verify(request).setAttribute("selectedCategory", "全部");
    }

    @Test
    void doGet_shouldSetEmptyStringWhenKeywordIsNull() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn(null);
        when(request.getParameter("q")).thenReturn(null);
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        when(service.search(null)).thenReturn(Collections.emptyList());
        when(service.extractCategories(anyList())).thenReturn(Collections.emptyList());

        controller.doGet(request, response);

        verify(request).setAttribute("keyword", "");
    }

    @Test
    void doGet_shouldTrimKeywordParameter() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn("  测试  ");
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> mockBooks = createMockBooks();
        when(service.search("测试")).thenReturn(mockBooks);
        when(service.extractCategories(mockBooks)).thenReturn(Collections.emptyList());

        controller.doGet(request, response);

        verify(service, times(2)).search("测试"); // 调用两次：一次用于搜索，一次用于提取分类
        verify(request).setAttribute("keyword", "测试");
    }

    @Test
    void doGet_shouldSetCategoriesAttribute() throws ServletException, IOException {
        when(request.getParameter("keyword")).thenReturn("测试");
        when(request.getParameter("category")).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp"))
                .thenReturn(requestDispatcher);

        List<BookInfo> mockBooks = createMockBooks();
        List<String> categories = Arrays.asList("小说", "文学", "历史");
        
        when(service.search("测试")).thenReturn(mockBooks);
        when(service.extractCategories(mockBooks)).thenReturn(categories);

        controller.doGet(request, response);

        verify(request).setAttribute("categories", categories);
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
     * 使用 Unsafe 分配实例，避免执行构造函数中的 Service 实例化。
     */
    private BookSearchController allocateWithoutConstructor() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        java.lang.reflect.Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BookSearchController) allocateInstance.invoke(unsafe, BookSearchController.class);
    }
}

