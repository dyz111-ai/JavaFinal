package com.example.demo0.reader.controller;

import com.example.demo0.reader.model.BookList;
import com.example.demo0.reader.service.BookListService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "BookListController", urlPatterns = "/reader/booklists")
public class BookListController extends HttpServlet {

    private final BookListService bookListService = new BookListService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 页面通过 API 异步加载数据，这里只需要转发页面
        request.getRequestDispatcher("/WEB-INF/views/reader/booklists.jsp").forward(request, response);
    }
}


