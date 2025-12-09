package com.example.demo0.book.controller;

import com.example.demo0.book.model.BookCategoryDetail;
import com.example.demo0.book.service.BookCategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 分类图书列表控制器
 * 显示某个分类下的所有图书
 */
@WebServlet(urlPatterns = {"/category/books"})
public class CategoryBooksController extends HttpServlet {

    private final BookCategoryService service = new BookCategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String categoryId = param(req, "categoryId");
        String categoryName = param(req, "categoryName");
        
        if (categoryId == null || categoryId.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少分类ID参数");
            return;
        }
        
        List<BookCategoryDetail> books = service.getCategoryBooks(categoryId);
        
        req.setAttribute("categoryId", categoryId);
        req.setAttribute("categoryName", categoryName != null ? categoryName : "未知分类");
        req.setAttribute("books", books);
        
        req.getRequestDispatcher("/WEB-INF/views/book/category-books.jsp").forward(req, resp);
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }
}

