package com.example.demo0.admin.controller;

import com.example.demo0.admin.service.BookCategoryAdminService;
import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.model.BookCategoryDetail;
import com.example.demo0.book.model.Category;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 管理员侧图书分类绑定 API
 * 路径: /api/admin/book-category/*
 * 功能:
 *  - GET /books?search=xx        搜索图书
 *  - GET /leaf-categories        获取所有叶子分类
 *  - GET /book/{isbn}            获取某本书的已绑定分类
 *  - POST /bind                  绑定图书与分类（覆盖式）
 */
@WebServlet("/api/admin/book-category/*")
public class BookCategoryApiController extends HttpServlet {

    private final BookCategoryAdminService service = new BookCategoryAdminService();
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo.startsWith("/book/") && pathInfo.length() > 6) {
                // 获取某本书的分类
                String isbn = pathInfo.substring(6);
                System.out.println("[BookCategoryApiController] 获取图书分类，ISBN: " + isbn);
                List<BookCategoryDetail> list = service.getBookCategories(isbn);
                System.out.println("[BookCategoryApiController] 返回分类数量: " + (list != null ? list.size() : 0));
                if (list != null && !list.isEmpty()) {
                    BookCategoryDetail d = list.get(0);
                    System.out.println("[BookCategoryApiController] 第一个分类: categoryId=" + d.getCategoryId() +
                            ", categoryName=" + d.getCategoryName() + ", path=" + d.getCategoryPath());
                }
                String json = gson.toJson(list != null ? list : Collections.emptyList());
                System.out.println("[BookCategoryApiController] 返回JSON: " + json.substring(0, Math.min(200, json.length())));
                out.print(json);
                return;
            } else if ("/leaf-categories".equals(pathInfo)) {
                System.out.println("[BookCategoryApiController] 获取叶子分类");
                List<Category> cats = service.getLeafCategories();
                System.out.println("[BookCategoryApiController] 叶子分类数量: " + (cats != null ? cats.size() : 0));
                out.print(gson.toJson(cats != null ? cats : Collections.emptyList()));
                return;
            } else if (pathInfo.equals("/books") || pathInfo.equals("") || pathInfo.equals("/")) {
                String search = req.getParameter("search");
                System.out.println("[BookCategoryApiController] 搜索图书, search=" + search);
                List<BookInfo> books = service.searchBooks(search);
                System.out.println("[BookCategoryApiController] 图书返回数量: " + (books != null ? books.size() : 0));
                if (books != null && !books.isEmpty()) {
                    BookInfo b = books.get(0);
                    System.out.println("[BookCategoryApiController] 首本: isbn=" + b.getISBN() +
                            ", title=" + b.getTitle() + ", author=" + b.getAuthor());
                }
                out.print(gson.toJson(books != null ? books : Collections.emptyList()));
                return;
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"无效的请求路径\"}");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        if ("/bind".equals(pathInfo)) {
            try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
                Map<String, Object> body = gson.fromJson(reader, Map.class);
                String isbn = body.get("isbn") != null ? body.get("isbn").toString() : null;
                List<String> categoryIds = null;
                if (body.get("categoryIds") instanceof List) {
                    categoryIds = (List<String>) body.get("categoryIds");
                }

                if (isbn == null || isbn.trim().isEmpty()) {
                    System.err.println("[BookCategoryApiController] ❌ 缺少ISBN");
                    resp.setStatus(400);
                    out.print("{\"error\":\"缺少ISBN\"}");
                    return;
                }

                System.out.println("[BookCategoryApiController] 绑定请求: isbn=" + isbn + ", categoryIds=" + categoryIds);
                boolean success = service.bindBookToCategories(isbn.trim(), categoryIds, "admin");
                out.print(gson.toJson(Map.of("success", success)));
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(500);
                resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"无效的请求路径\"}");
        }
    }
}

