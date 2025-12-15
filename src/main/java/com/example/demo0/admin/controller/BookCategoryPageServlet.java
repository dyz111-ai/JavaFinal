package com.example.demo0.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/book-category")
public class BookCategoryPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute("currentAdmin") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/book-category.jsp").forward(req, resp);
    }
}



