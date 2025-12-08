package com.example.demo0.reader.web;

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

@WebServlet(urlPatterns = {"/reader/*"})
public class ReaderServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = path(req);

        // 简单的权限检查：必须登录
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        switch (path) {
            case "/profile":
                req.getRequestDispatcher("/WEB-INF/views/reader/profile.jsp").forward(req, resp);
                break;
            case "/profile/edit": // 【新增】跳转到编辑页面
                req.getRequestDispatcher("/WEB-INF/views/reader/profile_edit.jsp").forward(req, resp);
                break;
            case "/borrow-records":
                req.getRequestDispatcher("/WEB-INF/views/reader/borrow_records.jsp").forward(req, resp);
                break;
            case "/booklists":
                req.getRequestDispatcher("/WEB-INF/views/reader/booklists.jsp").forward(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String path = path(req);

        // 权限检查
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        if ("/profile/edit".equals(path)) {
            handleProfileUpdate(req, resp, session);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleProfileUpdate(HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws ServletException, IOException {
        try {
            // 1. 获取表单数据
            String fullname = req.getParameter("fullname");
            String nickname = req.getParameter("nickname");

            // 2. 获取当前用户对象
            Reader currentUser = (Reader) session.getAttribute("currentUser");

            // 3. 更新对象属性
            currentUser.setFullname(fullname);
            currentUser.setNickname(nickname);

            // 4. 调用 Service 保存到数据库
            authService.updateProfile(currentUser);

            // 5. 更新 Session 中的用户信息（保持同步）
            session.setAttribute("currentUser", currentUser);

            // 6. 重定向回个人中心
            resp.sendRedirect(req.getContextPath() + "/reader/profile?msg=updated");

        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/reader/profile_edit.jsp").forward(req, resp);
        }
    }

    private String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null || p.isBlank()) ? "/profile" : p;
    }
}