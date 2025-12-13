package com.example.demo0.reader.controller;

import com.example.demo0.reader.service.BookListService;
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
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * BookListController 单元测试
 * 使用Mock隔离Service和Servlet依赖
 */
class BookListControllerTest {

    private BookListController controller;

    @Mock
    private BookListService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用 Unsafe 分配实例，避免执行构造函数中的 Service 实例化
        controller = allocateWithoutConstructor();
        // 注入 mock 的 service
        Field serviceField = BookListController.class.getDeclaredField("bookListService");
        serviceField.setAccessible(true);
        serviceField.set(controller, service);
    }

    @Test
    void doGet_shouldForwardToBookListsPage() throws ServletException, IOException {
        // 设置当请求转发到指定路径时返回 mock 的 RequestDispatcher
        when(request.getRequestDispatcher("/WEB-INF/views/reader/booklists.jsp"))
                .thenReturn(requestDispatcher);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证是否执行了正确的请求转发
        verify(request).getRequestDispatcher("/WEB-INF/views/reader/booklists.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    /**
     * 使用 Unsafe 分配实例，避免执行构造函数中的 Service 实例化
     */
    private BookListController allocateWithoutConstructor() throws Exception {
        // 获取 Unsafe 类
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        // 使用 Unsafe 分配实例
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BookListController) allocateInstance.invoke(unsafe, BookListController.class);
    }
}
