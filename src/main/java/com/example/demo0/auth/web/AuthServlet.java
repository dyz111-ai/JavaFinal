package com.example.demo0.auth.web;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.auth.service.AuthService;
import com.example.demo0.admin.entity.Librarian;
import com.example.demo0.admin.service.AdminAuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(urlPatterns = {"/auth/*"})
public class AuthServlet extends HttpServlet {

    private final AuthService readerAuthService = new AuthService();
    private final AdminAuthService adminAuthService = new AdminAuthService();

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
                // 获取登录类型
                String loginType = req.getParameter("loginType");
                String password = req.getParameter("password");

                if ("admin".equals(loginType)) {
                    // --- 管理员登录 ---
                    String staffNo = req.getParameter("staffNo");

                    if (staffNo == null || staffNo.isBlank() || password == null || password.isBlank()) {
                        throw new RuntimeException("工号和密码不能为空");
                    }

                    Librarian admin = adminAuthService.login(staffNo, password);

                    if (admin != null) {
                        req.getSession().setAttribute("currentUser", admin);
                        req.getSession().setAttribute("currentAdmin", admin);
                        req.getSession().setAttribute("userRole", "admin");

                        // 【关键】管理员登录成功后跳转到这里
                        // 确保您的项目中确实存在 /admin/dashboard 对应的 Servlet 或 JSP
                        // 如果没有，请先跳转到 /home 测试
                        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                    }
                } else {
                    // --- 普通读者登录 ---
                    String username = req.getParameter("username");

                    if (username == null || username.isBlank() || password == null || password.isBlank()) {
                        throw new RuntimeException("账号和密码不能为空");
                    }

                    Reader reader = readerAuthService.login(username, password);

                    if (reader != null) {
                        req.getSession().setAttribute("currentUser", reader);
                        req.getSession().setAttribute("userRole", "reader");
                        resp.sendRedirect(req.getContextPath() + "/home");
                    } else {
                        throw new RuntimeException("用户名或密码错误");
                    }
                }

            } else if ("/register".equals(path)) {
                String u = req.getParameter("username");
                String p = req.getParameter("password");
                String f = req.getParameter("fullname");
                String n = req.getParameter("nickname");

                if (u == null || u.isBlank() || p == null || p.isBlank()) {
                    throw new RuntimeException("用户名和密码不能为空");
                }

                readerAuthService.register(u, p, f, n);
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