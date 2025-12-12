package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.*;
import com.example.demo0.admin.service.BookAdminService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/books/*")
public class BookAdminApiController extends HttpServlet {

    private BookAdminService service;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("[BookAdminApiController] ========== Servlet初始化 ==========");
        System.out.println("[BookAdminApiController] 创建 BookAdminService 实例...");
        try {
            service = new BookAdminService();
            System.out.println("[BookAdminApiController] ✅ BookAdminService 创建成功");
        } catch (Exception e) {
            System.err.println("[BookAdminApiController] ❌ BookAdminService 创建失败: " + e.getMessage());
            e.printStackTrace();
        }
        gson = new Gson();
        System.out.println("[BookAdminApiController] ✅ Gson 创建成功");
        System.out.println("[BookAdminApiController] ========== Servlet初始化完成 ==========");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[BookAdminApiController] ========== 收到GET请求 ==========");
        resp.setContentType("application/json; charset=UTF-8");
        String search = req.getParameter("search");
        System.out.println("[BookAdminApiController] 搜索参数: " + search);

        try (PrintWriter out = resp.getWriter()) {
            System.out.println("[BookAdminApiController] 调用 service.getBooks()...");
            List<BookAdminDto> books = service.getBooks(search);
            System.out.println("[BookAdminApiController] ✅ 获取到 " + books.size() + " 条图书记录");
            
            String json = gson.toJson(books);
            System.out.println("[BookAdminApiController] 返回JSON长度: " + json.length());
            out.print(json);
        } catch (Exception e) {
            System.err.println("[BookAdminApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
        System.out.println("[BookAdminApiController] ========== GET请求处理完成 ==========");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            if ("/copies".equals(pathInfo)) {
                // 添加副本
                AddCopiesDto dto = gson.fromJson(reader, AddCopiesDto.class);
                service.addCopies(dto);
                out.print("{\"message\":\"副本添加成功\"}");
            } else {
                // 创建新书
                CreateBookDto dto = gson.fromJson(reader, CreateBookDto.class);
                service.createBook(dto);
                out.print("{\"message\":\"新书创建成功\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[BookAdminApiController] ========== 收到PUT请求 ==========");
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // 例如: /123/location 或 /123/takedown

        if (pathInfo == null || pathInfo.split("/").length < 2) {
            System.out.println("[BookAdminApiController] ❌ PathInfo格式错误: " + pathInfo);
            resp.setStatus(400);
            return;
        }

        String[] parts = pathInfo.split("/");
        Integer bookId = null;
        try {
            bookId = Integer.parseInt(parts[1]); // BookID
            System.out.println("[BookAdminApiController] BookID: " + bookId);
        } catch (NumberFormatException e) {
            System.out.println("[BookAdminApiController] ❌ BookID格式错误: " + parts[1]);
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"Invalid BookID\"}");
            return;
        }

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            if (pathInfo.endsWith("/location")) {
                // 更新位置
                System.out.println("[BookAdminApiController] 更新位置操作");
                Map<String, Object> map = gson.fromJson(reader, Map.class);
                Integer buildingId = map.get("buildingId") != null ? 
                    (map.get("buildingId") instanceof Number ? 
                        ((Number) map.get("buildingId")).intValue() : 
                        Integer.parseInt(map.get("buildingId").toString())) : null;
                Integer floor = map.get("floor") != null ? 
                    (map.get("floor") instanceof Number ? 
                        ((Number) map.get("floor")).intValue() : 
                        Integer.parseInt(map.get("floor").toString())) : null;
                String zone = map.get("zone") != null ? map.get("zone").toString() : null;
                
                System.out.println("[BookAdminApiController] 参数: buildingId=" + buildingId + ", floor=" + floor + ", zone=" + zone);
                
                boolean success = service.updateBookLocation(bookId, buildingId, floor, zone);
                out.print(gson.toJson(Map.of("success", success)));
            } else if (pathInfo.endsWith("/takedown")) {
                // 下架
                System.out.println("[BookAdminApiController] 下架操作");
                boolean success = service.takedownBook(bookId);
                out.print(gson.toJson(Map.of("success", success)));
            } else {
                // 更新基本信息（保留原有逻辑，使用ISBN）
                System.out.println("[BookAdminApiController] 更新基本信息操作");
                UpdateBookDto dto = gson.fromJson(reader, UpdateBookDto.class);
                // 这里需要从bookId获取isbn，暂时保留原逻辑
                resp.setStatus(400);
                out.print("{\"error\":\"更新基本信息功能暂不支持\"}");
            }
        } catch (Exception e) {
            System.err.println("[BookAdminApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
        System.out.println("[BookAdminApiController] ========== PUT请求处理完成 ==========");
    }
}