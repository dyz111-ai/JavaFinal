package com.example.demo0.reader.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 用于显示借还图书页面的Servlet
 */
@WebServlet(name = "BorrowReturnServlet", urlPatterns = "/reader/borrow-return")
public class BorrowReturnServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/reader/borrow_return.jsp").forward(request, response);
    }
}




