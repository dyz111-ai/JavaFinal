package com.example.demo0.auth.web;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {"/auth/*"})
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/login";

        switch (path) {
            case "/login":
                req.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(req, resp);
                break;
            case "/register":
                req.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(req, resp);
                break;
            case "/logout":
                req.getSession().invalidate();
                resp.sendRedirect(req.getContextPath() + "/home");
                break;
            default:
                resp.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = req.getPathInfo();

        try {
            if ("/login".equals(path)) {
                String u = req.getParameter("username");
                String p = req.getParameter("password");
                Reader reader = authService.login(u, p);

                if (reader != null) {
                    req.getSession().setAttribute("currentUser", reader);
                    resp.sendRedirect(req.getContextPath() + "/home");
                } else {
                    throw new RuntimeException("用户名或密码错误");
                }
            } else if ("/register".equals(path)) {
                String u = req.getParameter("username");
                String p = req.getParameter("password");
                String f = req.getParameter("fullname");
                String n = req.getParameter("nickname");

                if (u == null || u.isBlank() || p == null || p.isBlank()) {
                    throw new RuntimeException("用户名和密码不能为空");
                }

                authService.register(u, p, f, n);
                resp.sendRedirect(req.getContextPath() + "/auth/login?msg=registered");
            } else {
                resp.sendError(404);
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            String view = "/login".equals(path) ? "login.jsp" : "register.jsp";
            req.getRequestDispatcher("/WEB-INF/views/auth/" + view).forward(req, resp);
        }
    }
}