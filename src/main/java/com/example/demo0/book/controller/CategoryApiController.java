package com.example.demo0.book.controller;

import com.example.demo0.book.model.Category;
import com.example.demo0.book.model.CategoryNode;
import com.example.demo0.book.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 分类查询REST API控制器（读者使用）
 * 对应原项目的 CategoryController
 * 
 * API端点：
 * - GET /api/Category/tree - 获取分类树
 * - GET /api/Category/{id} - 获取单个分类
 */
@WebServlet(urlPatterns = {"/api/Category/*"})
public class CategoryApiController extends HttpServlet {

    private final CategoryService service = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";
        
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        
        try (PrintWriter out = resp.getWriter()) {
            if ("/tree".equals(pathInfo)) {
                // 获取分类树
                List<CategoryNode> tree = service.getCategoryTree();
                writeCategoryTreeJson(out, tree);
            } else if (pathInfo.startsWith("/") && pathInfo.length() > 1) {
                // 获取单个分类 /api/Category/{id}
                String categoryId = pathInfo.substring(1);
                Category category = service.findById(categoryId);
                if (category == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"message\":\"分类不存在\"}");
                } else {
                    writeCategoryJson(out, category);
                }
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


    private void writeCategoryTreeJson(PrintWriter out, List<CategoryNode> nodes) {
        out.print("[");
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) out.print(",");
            writeCategoryNodeJson(out, nodes.get(i));
        }
        out.print("]");
    }

    private void writeCategoryNodeJson(PrintWriter out, CategoryNode node) {
        out.print("{");
        out.print("\"CategoryID\":" + quote(node.getCategoryId()));
        out.print(",\"CategoryName\":" + quote(node.getCategoryName()));
        out.print(",\"ParentCategoryID\":" + quote(node.getParentCategoryId()));
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            out.print(",\"Children\":[");
            for (int i = 0; i < node.getChildren().size(); i++) {
                if (i > 0) out.print(",");
                writeCategoryNodeJson(out, node.getChildren().get(i));
            }
            out.print("]");
        } else {
            out.print(",\"Children\":[]");
        }
        out.print("}");
    }

    private void writeCategoryJson(PrintWriter out, Category category) {
        out.print("{");
        out.print("\"CategoryID\":" + quote(category.getCategoryId()));
        out.print(",\"CategoryName\":" + quote(category.getCategoryName()));
        out.print(",\"ParentCategoryID\":" + quote(category.getParentCategoryId()));
        out.print("}");
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

