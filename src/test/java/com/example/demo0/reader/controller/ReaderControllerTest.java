package com.example.demo0.reader.controller;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.auth.service.AuthService;
import com.example.demo0.reader.model.BorrowRecordDetail;
import com.example.demo0.reader.service.BorrowingService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * ReaderController 单元测试
 * 使用Mock隔离Service和Servlet依赖
 */
class ReaderControllerTest {

    private ReaderController controller;

    @Mock
    private BorrowingService borrowingService;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher requestDispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        // 使用 Unsafe 分配实例，避免执行构造函数中的 Service 实例化
        controller = allocateWithoutConstructor();
        // 注入 mock 的 service
        Field borrowingServiceField = ReaderController.class.getDeclaredField("borrowingService");
        borrowingServiceField.setAccessible(true);
        borrowingServiceField.set(controller, borrowingService);
        
        Field authServiceField = ReaderController.class.getDeclaredField("authService");
        authServiceField.setAccessible(true);
        authServiceField.set(controller, authService);
    }

    @Test
    void doGet_shouldForwardToProfilePage() throws ServletException, IOException {
        // 设置当请求转发到指定路径时返回 mock 的 RequestDispatcher
        when(request.getPathInfo()).thenReturn(null);
        when(request.getRequestDispatcher("/WEB-INF/views/reader/profile.jsp")).thenReturn(requestDispatcher);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证是否执行了正确的请求转发
        verify(request).getRequestDispatcher("/WEB-INF/views/reader/profile.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_shouldForwardToProfileEditPage() throws ServletException, IOException {
        // 设置当请求转发到指定路径时返回 mock 的 RequestDispatcher
        when(request.getPathInfo()).thenReturn("/profile/edit");
        when(request.getRequestDispatcher("/WEB-INF/views/reader/profile_edit.jsp")).thenReturn(requestDispatcher);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证是否执行了正确的请求转发
        verify(request).getRequestDispatcher("/WEB-INF/views/reader/profile_edit.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doGet_shouldLoadBorrowRecords() throws ServletException, IOException {
        // 准备测试数据
        Reader reader = createReader(1, "测试用户");
        BorrowRecordDetail record = createBorrowRecordDetail(1, "9781234567890", "测试图书", "测试作者", "1", "测试用户");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn("/borrow-records");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(reader);
        when(borrowingService.findByReaderId("1")).thenReturn(Arrays.asList(record));
        when(borrowingService.getUnreturnedCountByReader("1")).thenReturn(5);
        when(borrowingService.getOverdueUnreturnedCountByReader("1")).thenReturn(2);
        when(request.getRequestDispatcher("/WEB-INF/views/reader/borrow_records.jsp")).thenReturn(requestDispatcher);

        // 调用被测试方法
        controller.doGet(request, response);

        // 验证结果
        verify(borrowingService).findByReaderId("1");
        verify(borrowingService).getUnreturnedCountByReader("1");
        verify(borrowingService).getOverdueUnreturnedCountByReader("1");
        verify(request).setAttribute("records", Arrays.asList(record));
        verify(request).setAttribute("unreturnedCount", 5);
        verify(request).setAttribute("overdueCount", 2);
        verify(request).getRequestDispatcher("/WEB-INF/views/reader/borrow_records.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void doPost_shouldHandleProfileUpdateSuccess() throws ServletException, IOException {
        // 准备测试数据
        Reader reader = createReader(1, "测试用户");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn("/profile/edit");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(reader);
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("fullname")).thenReturn("测试用户");
        when(request.getParameter("nickname")).thenReturn("test");
        when(request.getParameter("avatar")).thenReturn("/avatars/test.jpg");
        when(request.getContextPath()).thenReturn("/demo0");

        // 调用被测试方法
        controller.doPost(request, response);

        // 验证结果
        verify(authService).updateProfile(reader);
        verify(session).setAttribute("currentUser", reader);
        verify(response).sendRedirect("/demo0/reader/profile?msg=updated");
    }

    @Test
    void doPost_shouldHandleProfileUpdateFailure() throws ServletException, IOException {
        // 准备测试数据
        Reader reader = createReader(1, "测试用户");
        
        // 设置 mock 行为
        when(request.getPathInfo()).thenReturn("/profile/edit");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(reader);
        when(request.getParameter("username")).thenReturn("testuser");
        when(request.getParameter("fullname")).thenReturn("测试用户");
        when(request.getParameter("nickname")).thenReturn("test");
        when(request.getParameter("avatar")).thenReturn("/avatars/test.jpg");
        when(request.getContextPath()).thenReturn("/demo0");
        doThrow(new RuntimeException("更新失败")).when(authService).updateProfile(reader);

        // 调用被测试方法
        controller.doPost(request, response);

        // 验证结果
        verify(authService).updateProfile(reader);
        verify(session).setAttribute("profileUpdateError", "更新失败");
        verify(response).sendRedirect("/demo0/reader/profile/edit?error=1");
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
    private ReaderController allocateWithoutConstructor() throws Exception {
        // 获取 Unsafe 类
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Object unsafe = theUnsafe.get(null);

        // 使用 Unsafe 分配实例
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (ReaderController) allocateInstance.invoke(unsafe, ReaderController.class);
    }
}