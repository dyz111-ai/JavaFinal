package com.example.demo0.book.controller;

import com.example.demo0.book.service.PhysicalBookService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 实体书查询控制器
 * 对应原项目的 BookShelfController.SearchBookWhichShelf 功能
 */
@WebServlet(urlPatterns = {"/book/physical"})
public class PhysicalBookController extends HttpServlet {

    private final PhysicalBookService service = new PhysicalBookService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = param(req, "keyword");
        String isbn = param(req, "isbn");

        // 参数验证
        if ((isbn == null || isbn.isBlank()) && (keyword == null || keyword.isBlank())) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 keyword 或 isbn 参数");
            return;
        }

        // 通过Service层查询
        PhysicalBookService.PhysicalBookQueryResult result = service.queryPhysicalBooks(keyword, isbn);

        req.setAttribute("books", result.getBooks());
        req.setAttribute("bookTitle", result.getBookTitle());
        req.getRequestDispatcher("/WEB-INF/views/book/physical-list.jsp").forward(req, resp);
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }
}

