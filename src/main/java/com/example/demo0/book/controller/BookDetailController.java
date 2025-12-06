package com.example.demo0.book.controller;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.repository.BookRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/book/detail"})
public class BookDetailController extends HttpServlet {

    private final BookRepository repository = new BookRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String isbn = param(req, "isbn");
        if (isbn == null || isbn.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "缺少 isbn 参数");
            return;
        }
        BookInfo book = repository.findByIsbn(isbn.trim());
        if (book == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "未找到该图书");
            return;
        }
        req.setAttribute("book", book);
        req.getRequestDispatcher("/WEB-INF/views/book/detail.jsp").forward(req, resp);
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }
}

