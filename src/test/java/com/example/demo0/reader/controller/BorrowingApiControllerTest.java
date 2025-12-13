package com.example.demo0.reader.controller;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.reader.model.BorrowRecordDetail;
import com.example.demo0.reader.service.BorrowingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.*;

/**
 * BorrowingApiController 单元测试
 * 使用Mock隔离Service和Servlet依赖
 */
class BorrowingApiControllerTest {

    private BorrowingApiController controller;

    @Mock
    private BorrowingService service;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用 Unsafe 分配实例，避免执行构造函数中的 Service 实例化
        controller = allocateWithoutConstructor();
        // 注入 mock 的 service
        Field serviceField = BorrowingApiController.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(controller, service);
    }

    @Test
    void doGet_shouldHandleReaderPath() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/reader";
        Reader reader = createReader(1, "测试用户");
        BorrowRecordDetail record = createBorrowRecordDetail(1, "9781234567890", "测试图书", "测试作者", "1", "测试用户");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(reader);
        when(service.findByReaderId("1")).thenReturn(Arrays.asList(record));
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(service).findByReaderId("1");
        verify(printWriter).print("[");
        verify(printWriter).print(contains("BorrowRecordID"));
        verify(printWriter).print("]");
    }

    @Test
    void doGet_shouldHandleReaderPathWhenNotLoggedIn() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/reader";
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(printWriter).print(contains("未登录或登录状态已失效"));
        verify(service, never()).findByReaderId(anyString());
    }

    @Test
    void doGet_shouldHandleIdPath() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/123";
        BorrowRecordDetail record = createBorrowRecordDetail(123, "9781234567890", "测试图书", "测试作者", "1", "测试用户");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(service.findById(123)).thenReturn(record);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(service).findById(123);
        verify(printWriter).print(contains("BorrowRecordID"));
    }

    @Test
    void doGet_shouldHandleIdPathWhenRecordNotFound() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/123";
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(service.findById(123)).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(service).findById(123);
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(printWriter).print(contains("借阅记录不存在"));
    }

    @Test
    void doGet_shouldHandleUnreturnedCountPath() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/unreturned-count/1";
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(service.getUnreturnedCountByReader("1")).thenReturn(5);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(service).getUnreturnedCountByReader("1");
        verify(printWriter).print("{\"count\":5}");
    }

    @Test
    void doGet_shouldHandleOverdueCountPath() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/overdue-unreturned-count/1";
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(service.getOverdueUnreturnedCountByReader("1")).thenReturn(2);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(service).getOverdueUnreturnedCountByReader("1");
        verify(printWriter).print("{\"count\":2}");
    }

    @Test
    void doPost_shouldHandleBorrow() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/borrow";
        Reader reader = createReader(1, "测试用户");
        BorrowingService.BorrowResult result = new BorrowingService.BorrowResult(true, "借阅成功", "测试数据");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(reader);
        when(request.getParameter("barcode")).thenReturn("1234567890");
        when(service.borrowBook("1", "1234567890")).thenReturn(result);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doPost(request, response);

        // 验证结果
        verify(service).borrowBook("1", "1234567890");
        verify(printWriter).print(contains("\"success\":true"));
        verify(printWriter).print(contains("借阅成功"));
    }

    @Test
    void doPost_shouldHandleReturn() throws ServletException, IOException {
        // 准备测试数据
        String pathInfo = "/return";
        Reader reader = createReader(1, "测试用户");
        BorrowingService.ReturnResult result = new BorrowingService.ReturnResult(true, "归还成功");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(reader);
        when(request.getParameter("barcode")).thenReturn("1234567890");
        when(service.returnBook("1", "1234567890")).thenReturn(result);
        when(response.getWriter()).thenReturn(printWriter);

        // 调用被测试方法
        controller.doPost(request, response);

        // 验证结果
        verify(service).returnBook("1", "1234567890");
        verify(printWriter).print(contains("\"success\":true"));
        verify(printWriter).print(contains("归还成功"));
    }

    private Reader createReader(int id, String nickname) {
        Reader reader = new Reader();
        reader.setReaderId(id);
        reader.setNickname(nickname);
        return reader;
    }

    private BorrowRecordDetail createBorrowRecordDetail(int id, String isbn, String title, String author, String readerId, String readerName) {
        BorrowRecordDetail record = new BorrowRecordDetail();
        record.setBorrowRecordId(id);
        record.setIsbn(isbn);
        record.setBookTitle(title);
        record.setBookAuthor(author);
        record.setReaderId(readerId);
        record.setReaderName(readerName);
        record.setBorrowTime(OffsetDateTime.now());
        return record;
    }

    /**
     * 使用 Unsafe 分配实例，避免执行构造函数中的 Service 实例化
     */
    private BorrowingApiController allocateWithoutConstructor() throws Exception {
        // 获取 Unsafe 类
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        // 使用 Unsafe 分配实例
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (BorrowingApiController) allocateInstance.invoke(unsafe, BorrowingApiController.class);
    }
}
