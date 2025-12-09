package com.example.demo0.book.controller;

import com.example.demo0.book.model.BookCategoryDetail;
import com.example.demo0.book.model.Category;
import com.example.demo0.book.service.BookCategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 图书分类关联查询REST API控制器（读者使用）
 * 对应原项目的 BookCategoryController
 * 
 * API端点：
 * - GET /api/BookCategory/book/{isbn} - 获取图书的所有分类
 * - GET /api/BookCategory/category/{categoryId} - 获取分类下的所有图书
 * - GET /api/BookCategory/leaf-categories - 获取所有叶子节点分类
 */
@WebServlet(urlPatterns = {"/api/BookCategory/*"})
public class BookCategoryApiController extends HttpServlet {

    private final BookCategoryService service = new BookCategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        
        try (PrintWriter out = resp.getWriter()) {
            if ("/leaf-categories".equals(pathInfo)) {
                // 获取所有叶子节点分类
                List<Category> categories = service.getLeafCategories();
                writeCategoryListJson(out, categories);
            } else if ("/stats".equals(pathInfo)) {
                // 获取分类统计（暂时返回空，后续实现）
                out.print("{}");
            } else if (pathInfo.startsWith("/book/") && pathInfo.length() > 6) {
                // 获取图书的所有分类 /api/BookCategory/book/{isbn}
                String isbn = pathInfo.substring(6);
                List<BookCategoryDetail> details = service.getBookCategories(isbn);
                writeBookCategoryDetailListJson(out, details);
            } else if (pathInfo.startsWith("/category/") && pathInfo.length() > 10) {
                // 获取分类下的所有图书 /api/BookCategory/category/{categoryId}
                String categoryId = pathInfo.substring(10);
                List<BookCategoryDetail> details = service.getCategoryBooks(categoryId);
                writeBookCategoryDetailListJson(out, details);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"message\":\"无效的请求路径\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }


    private void writeCategoryListJson(PrintWriter out, List<Category> categories) {
        out.print("[");
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) out.print(",");
            Category c = categories.get(i);
            out.print("{");
            out.print("\"CategoryID\":" + quote(c.getCategoryId()));
            out.print(",\"CategoryName\":" + quote(c.getCategoryName()));
            // 获取分类路径
            List<String> path = service.getCategoryPath(c.getCategoryId());
            out.print(",\"CategoryPath\":" + quote(String.join(" / ", path)));
            out.print("}");
        }
        out.print("]");
    }

    private void writeBookCategoryDetailListJson(PrintWriter out, List<BookCategoryDetail> details) {
        out.print("[");
        for (int i = 0; i < details.size(); i++) {
            if (i > 0) out.print(",");
            BookCategoryDetail d = details.get(i);
            out.print("{");
            out.print("\"ISBN\":" + quote(d.getIsbn()));
            out.print(",\"Title\":" + quote(d.getTitle()));
            out.print(",\"Author\":" + quote(d.getAuthor()));
            out.print(",\"CategoryID\":" + quote(d.getCategoryId()));
            out.print(",\"CategoryName\":" + quote(d.getCategoryName()));
            out.print(",\"CategoryPath\":" + quote(d.getCategoryPath()));
            out.print(",\"RelationNote\":" + quote(d.getRelationNote()));
            out.print("}");
        }
        out.print("]");
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    private String quote(String s) {
        if (s == null) return "null";
        return "\"" + escapeJson(s) + "\"";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

