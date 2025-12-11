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

    private final BookAdminService service = new BookAdminService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String search = req.getParameter("search");

        try (PrintWriter out = resp.getWriter()) {
            List<BookAdminDto> books = service.getBooks(search);
            out.print(gson.toJson(books));
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
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
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // 例如: /978xxx/location

        if (pathInfo == null || pathInfo.split("/").length < 2) {
            resp.setStatus(400);
            return;
        }

        String[] parts = pathInfo.split("/");
        String isbn = parts[1]; // ISBN

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            if (pathInfo.endsWith("/location")) {
                // 更新位置
                Map<String, String> map = gson.fromJson(reader, Map.class);
                String location = map.get("location");
                boolean success = service.updateBookLocation(isbn, location);
                out.print(gson.toJson(Map.of("success", success)));
            } else if (pathInfo.endsWith("/takedown")) {
                // 下架
                boolean success = service.takedownBook(isbn);
                out.print(gson.toJson(Map.of("success", success)));
            } else {
                // 更新基本信息
                UpdateBookDto dto = gson.fromJson(reader, UpdateBookDto.class);
                // 确保 DTO 里有 ISBN
                if(dto.getIsbn() == null) dto.setIsbn(isbn);
                boolean success = service.updateBookInfo(isbn, dto);
                out.print(gson.toJson(Map.of("success", success)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}