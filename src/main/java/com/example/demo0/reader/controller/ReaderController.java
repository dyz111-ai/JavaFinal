package com.example.demo0.reader.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {"/reader/*"})
public class ReaderController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = path(req);
        switch (path) {
            case "/profile":
                forward(req, resp, "/WEB-INF/views/reader/profile.jsp");
                break;
            case "/borrow-records":
                forward(req, resp, "/WEB-INF/views/reader/borrow_records.jsp");
                break;
            case "/booklists":
                forward(req, resp, "/WEB-INF/views/reader/booklists.jsp");
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    private String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null || p.isBlank()) ? "/profile" : p;
    }
}



