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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 简单权限检查
        if (req.getSession().getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        // 转发到受保护的 JSP
        req.getRequestDispatcher("/WEB-INF/views/admin/books.jsp").forward(req, resp);
    }
}