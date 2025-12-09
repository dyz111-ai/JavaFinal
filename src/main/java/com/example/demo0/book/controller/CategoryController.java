package com.example.demo0.book.controller;

import com.example.demo0.book.model.CategoryNode;
import com.example.demo0.book.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 分类页面控制器
 * 处理分类显示页面的请求（读者查看）
 */
@WebServlet(urlPatterns = {"/category/display"})
public class CategoryController extends HttpServlet {

    private final CategoryService service = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 分类显示页面（读者查看）
        List<CategoryNode> tree = service.getCategoryTree();
        req.setAttribute("categoryTree", tree);
        req.getRequestDispatcher("/WEB-INF/views/book/category-display.jsp").forward(req, resp);
    }
}

