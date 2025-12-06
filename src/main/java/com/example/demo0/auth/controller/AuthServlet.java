package com.example.demo0.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {"/auth/*"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = path(req);
        switch (path) {
            case "/login":
                forward(req, resp, "/WEB-INF/views/auth/login.jsp");
                break;
            case "/register":
                forward(req, resp, "/WEB-INF/views/auth/register.jsp");
                break;
            case "/profile":
                forward(req, resp, "/WEB-INF/views/auth/profile.jsp");
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
        return (p == null || p.isBlank()) ? "/login" : p;
    }
}



