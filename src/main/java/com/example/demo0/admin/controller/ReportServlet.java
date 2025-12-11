package com.example.demo0.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 举报处理页面控制器
 * 负责处理/admin/report-handling路径的请求
 * 将请求转发到举报处理前端页面
 */
@WebServlet("/admin/report-handling")
public class ReportServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 处理GET请求，显示举报处理页面
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 权限检查：只有登录的管理员才能访问
        if (request.getSession().getAttribute("currentAdmin") == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
        
        // 设置字符编码，确保中文正常显示
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html; charset=UTF-8");
        
        // 转发到举报处理页面
        request.getRequestDispatcher("/WEB-INF/views/admin/report-handling.jsp").forward(request, response);
    }
    
    /**
     * 处理POST请求
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 暂时重定向到GET方法，保持页面一致性
        doGet(request, response);
    }
}
