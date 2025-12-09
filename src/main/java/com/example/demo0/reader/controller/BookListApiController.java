package com.example.demo0.reader.controller;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.reader.model.BookList;
import com.example.demo0.reader.service.BookListService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/book/booklists/*"})
public class BookListApiController extends HttpServlet {

    private final BookListService bookListService = new BookListService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置请求和响应的字符编码为 UTF-8
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try (PrintWriter out = resp.getWriter()) {
            // GET /api/book/booklists/reader/{readerId}
            if (pathInfo.startsWith("/reader")) {
                // 从路径中提取readerId，格式：/reader/1 或 /reader
                Integer readerId = 1; // 默认值
                if (pathInfo.length() > "/reader".length()) {
                    // 有具体的readerId，格式：/reader/1
                    String readerIdStr = pathInfo.substring("/reader/".length());
                    try {
                        readerId = Integer.parseInt(readerIdStr);
                    } catch (NumberFormatException e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(Map.of("error", "无效的读者ID: " + readerIdStr)));
                        return;
                    }
                }
                try {
                    // 返回格式：{ "Created": [...], "Collected": [...] }
                    Map<String, List<BookList>> result = bookListService.searchBooklistsByReader(readerId);
                    out.print(gson.toJson(result));
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "查询失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // GET /api/book/booklists/{booklistId}
            if (pathInfo.matches("/\\d+")) {
                Integer booklistId = Integer.parseInt(pathInfo.substring(1));
                try {
                    BookList booklist = bookListService.getBookListDetails(booklistId);
                    if (booklist == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print(gson.toJson(Map.of("message", "书单未找到")));
                        return;
                    }
                    List<BookInfo> books = bookListService.getBooksInBookList(booklistId);
                    // 返回格式：{ "BooklistInfo": {...}, "Books": [...] }
                    Map<String, Object> response = new HashMap<>();
                    response.put("BooklistInfo", booklist);
                    response.put("Books", books);
                    out.print(gson.toJson(response));
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "查询失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // GET /api/book/booklists/{booklistId}/recommend
            if (pathInfo.matches("/\\d+/recommend")) {
                String[] parts = pathInfo.split("/");
                Integer booklistId = Integer.parseInt(parts[1]);
                String limitStr = req.getParameter("limit");
                Integer limit = limitStr != null ? Integer.parseInt(limitStr) : 10;
                try {
                    List<BookList> recommendations = bookListService.recommendBooklists(booklistId, limit);
                    Map<String, Object> response = new HashMap<>();
                    response.put("Items", recommendations);
                    out.print(gson.toJson(response));
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "推荐失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("message", "无效的请求")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置请求和响应的字符编码为 UTF-8
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try (PrintWriter out = resp.getWriter()) {
            Integer readerId = 1; // Hardcoded for now

            // POST /api/book/booklists (Create new booklist)
            if (pathInfo == null || pathInfo.equals("") || pathInfo.equals("/")) {
                String name = req.getParameter("name");
                String introduction = req.getParameter("introduction");
                
                if (name == null || name.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(Map.of("error", "书单名称不能为空")));
                    return;
                }
                
                try {
                    BookList createdBookList = bookListService.createBookList(name, introduction != null ? introduction : "", readerId);
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                    out.print(gson.toJson(createdBookList));
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "创建书单失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // POST /api/book/booklists/{booklistId}/books
            if (pathInfo.matches("/\\d+/books")) {
                Integer booklistId = Integer.parseInt(pathInfo.split("/")[1]);
                String isbn = req.getParameter("isbn");
                try {
                    boolean success = bookListService.addBookToBookList(booklistId, isbn, readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "添加成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "添加失败，可能无权限或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "添加失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // POST /api/book/booklists/{booklistId}/collect
            if (pathInfo.matches("/\\d+/collect")) {
                Integer booklistId = Integer.parseInt(pathInfo.split("/")[1]);
                String notes = req.getParameter("notes");
                try {
                    boolean success = bookListService.collectBooklist(booklistId, readerId, notes);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "收藏成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "收藏失败，可能已收藏或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "收藏失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("message", "无效的请求")));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置请求和响应的字符编码为 UTF-8
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try (PrintWriter out = resp.getWriter()) {
            Integer readerId = 1; // Hardcoded for now

            // PUT /api/book/booklists/{booklistId}
            if (pathInfo.matches("/\\d+")) {
                Integer booklistId = Integer.parseInt(pathInfo.substring(1));
                String name = req.getParameter("name");
                String introduction = req.getParameter("introduction");
                try {
                    boolean success = bookListService.updateBookListInfo(booklistId, name, introduction, readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "更新成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "更新失败，可能无权限或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "更新失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // PUT /api/book/booklists/{booklistId}/name
            if (pathInfo.matches("/\\d+/name")) {
                Integer booklistId = Integer.parseInt(pathInfo.split("/")[1]);
                String newName = req.getParameter("NewName");
                if (newName == null || newName.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "书单名称不能为空")));
                    return;
                }
                try {
                    boolean success = bookListService.updateBooklistName(booklistId, newName, readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "更新成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "更新失败，可能无权限或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "更新失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // PUT /api/book/booklists/{booklistId}/intro
            if (pathInfo.matches("/\\d+/intro")) {
                Integer booklistId = Integer.parseInt(pathInfo.split("/")[1]);
                String newIntro = req.getParameter("NewIntro");
                try {
                    boolean success = bookListService.updateBooklistIntroduction(booklistId, newIntro != null ? newIntro : "", readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "更新成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "更新失败，可能无权限或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "更新失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // PUT /api/book/booklists/{booklistId}/collect/notes
            if (pathInfo.matches("/\\d+/collect/notes")) {
                Integer booklistId = Integer.parseInt(pathInfo.split("/")[1]);
                String newNotes = req.getParameter("NewNotes");
                try {
                    boolean success = bookListService.updateCollectNotes(booklistId, readerId, newNotes != null ? newNotes : "");
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "更新成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "更新失败，可能未收藏该书单")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "更新失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("message", "无效的请求")));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置请求和响应的字符编码为 UTF-8
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try (PrintWriter out = resp.getWriter()) {
            Integer readerId = 1; // Hardcoded for now

            // DELETE /api/book/booklists/{booklistId}
            if (pathInfo.matches("/\\d+")) {
                Integer booklistId = Integer.parseInt(pathInfo.substring(1));
                try {
                    boolean success = bookListService.deleteBookList(booklistId, readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("message", "删除成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(Map.of("message", "删除失败，可能无权限或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("error", "删除失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // DELETE /api/book/booklists/{booklistId}/books/{isbn}
            if (pathInfo.matches("/\\d+/books/.*?")) {
                String[] parts = pathInfo.split("/");
                Integer booklistId = Integer.parseInt(parts[1]);
                String isbn = parts[3];
                try {
                    boolean success = bookListService.removeBookFromBookList(booklistId, isbn, readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "移除成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "移除失败，可能无权限或书单不存在")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "移除失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            // DELETE /api/book/booklists/{booklistId}/collect
            if (pathInfo.matches("/\\d+/collect")) {
                Integer booklistId = Integer.parseInt(pathInfo.split("/")[1]);
                try {
                    boolean success = bookListService.cancelCollectBooklist(booklistId, readerId);
                    if (success) {
                        out.print(gson.toJson(Map.of("Success", 1, "message", "取消收藏成功")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print(gson.toJson(Map.of("Success", 0, "message", "取消收藏失败，可能未收藏该书单")));
                    }
                } catch (SQLException e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print(gson.toJson(Map.of("Success", 0, "error", "取消收藏失败: " + e.getMessage())));
                    e.printStackTrace();
                }
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("message", "无效的请求")));
        }
    }
}

