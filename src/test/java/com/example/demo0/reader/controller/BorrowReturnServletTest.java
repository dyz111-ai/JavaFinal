package com.example.demo0.reader.controller;

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
 * BorrowReturnServlet 单元测试
 * 使用Mock隔离Servlet依赖
 */
class BorrowReturnServletTest {

    private BorrowReturnServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用 Unsafe 分配实例
        servlet = allocateWithoutConstructor();
    }

    @Test
    void doGet_shouldForwardToBorrowReturnPage() throws ServletException, IOException {
        // 设置当请求转发到指定路径时返回 mock 的 RequestDispatcher
        when(request.getRequestDispatcher("/WEB-INF/views/reader/borrow_return.jsp"))
                .thenReturn(requestDispatcher);

        // 调用被测试方法
        servlet.doGet(request, response);

        // 验证是否执行了正确的请求转发
        verify(request).getRequestDispatcher("/WEB-INF/views/reader/borrow_return.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    /**
     * 使用 Unsafe 分配实例，避免执行构造函数
     */
    private BorrowReturnServlet allocateWithoutConstructor() throws Exception {
        // 获取 Unsafe 类
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        // 使用 Unsafe 分配实例
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BorrowReturnServlet) allocateInstance.invoke(unsafe, BorrowReturnServlet.class);
    }
}