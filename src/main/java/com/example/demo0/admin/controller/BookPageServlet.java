package com.example.demo0.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// 映射 URL 路径 /admin/books
@WebServlet("/admin/books")
public class BookPageServlet extends HttpServlet {
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("[BookPageServlet] ========== Servlet初始化 ==========");
        System.out.println("[BookPageServlet] ✅ BookPageServlet 已加载");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[BookPageServlet] ========== 收到GET请求 ==========");
        System.out.println("[BookPageServlet] 请求URI: " + req.getRequestURI());
        System.out.println("[BookPageServlet] 会话ID: " + req.getSession().getId());
        
        // 简单权限检查
        Object currentAdmin = req.getSession().getAttribute("currentAdmin");
        System.out.println("[BookPageServlet] currentAdmin: " + (currentAdmin != null ? "已登录" : "未登录"));
        
        if (currentAdmin == null) {
            System.out.println("[BookPageServlet] ❌ 未登录，重定向到登录页面");
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        
        System.out.println("[BookPageServlet] ✅ 权限检查通过，转发到 books.jsp");
        // 转发到受保护的 JSP
        req.getRequestDispatcher("/WEB-INF/views/admin/books.jsp").forward(req, resp);
        System.out.println("[BookPageServlet] ========== 请求处理完成 ==========");
    }
}