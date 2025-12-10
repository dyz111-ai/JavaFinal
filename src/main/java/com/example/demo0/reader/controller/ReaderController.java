package com.example.demo0.reader.controller;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.auth.service.AuthService;
import com.example.demo0.reader.model.BorrowRecordDetail;
import com.example.demo0.reader.service.BorrowingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(urlPatterns = {"/reader/*"})
@MultipartConfig
public class ReaderController extends HttpServlet {

    private final BorrowingService borrowingService = new BorrowingService();
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = path(req);
        switch (path) {
            case "/profile":
                forward(req, resp, "/WEB-INF/views/reader/profile.jsp");
                break;
            case "/profile/edit":
                forward(req, resp, "/WEB-INF/views/reader/profile_edit.jsp");
                break;
            case "/borrow-records":
                // 加载借阅记录数据
                loadBorrowRecords(req);
                forward(req, resp, "/WEB-INF/views/reader/borrow_records.jsp");
                break;
            case "/booklists":
                forward(req, resp, "/WEB-INF/views/reader/booklists.jsp");
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = path(req);
        switch (path) {
            case "/profile/edit":
                handleProfileUpdate(req, resp);
                break;
            case "/profile/avatar":
                handleAvatarUpdate(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleProfileUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String username = req.getParameter("username");
        String fullname = req.getParameter("fullname");
        String nickname = req.getParameter("nickname");
        String avatar = req.getParameter("avatar");

        currentUser.setUsername(username);
        currentUser.setFullname(fullname);
        currentUser.setNickname(nickname);
        currentUser.setAvatar(avatar);

        try {
            authService.updateProfile(currentUser);
            // 同步 session 中的用户信息
            req.getSession().setAttribute("currentUser", currentUser);
            resp.sendRedirect(req.getContextPath() + "/reader/profile?msg=updated");
        } catch (RuntimeException e) {
            // 带上错误信息返回编辑页
            req.getSession().setAttribute("profileUpdateError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
        }
    }

    private void handleAvatarUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        Part filePart;
        try {
            filePart = req.getPart("avatarFile");
        } catch (Exception e) {
            req.getSession().setAttribute("profileUpdateError", "上传失败");
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            req.getSession().setAttribute("profileUpdateError", "请上传头像文件");
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
            return;
        }

        String submittedFileName = filePart.getSubmittedFileName();
        if (submittedFileName == null) {
            req.getSession().setAttribute("profileUpdateError", "文件名无效");
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
            return;
        }

        String lower = submittedFileName.toLowerCase();
        if (!(lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png"))) {
            req.getSession().setAttribute("profileUpdateError", "仅支持 jpg/jpeg/png 格式");
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
            return;
        }

        if (filePart.getSize() > 1 * 1024 * 1024) { // 1MB
            req.getSession().setAttribute("profileUpdateError", "文件大小不能超过 1MB");
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
            return;
        }

        String ext = lower.substring(lower.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "-" + Math.abs(submittedFileName.hashCode()) + ext;
        java.nio.file.Path uploadDir = java.nio.file.Paths.get(req.getServletContext().getRealPath("/uploads/avatars"));
        try {
            java.nio.file.Files.createDirectories(uploadDir);
            java.nio.file.Path dest = uploadDir.resolve(fileName);
            try (java.io.InputStream in = filePart.getInputStream()) {
                java.nio.file.Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            String relativePath = req.getContextPath() + "/uploads/avatars/" + fileName;
            currentUser.setAvatar(relativePath);
            authService.updateProfile(currentUser);
            req.getSession().setAttribute("currentUser", currentUser);
            resp.sendRedirect(req.getContextPath() + "/reader/profile?msg=updated");
        } catch (Exception e) {
            req.getSession().setAttribute("profileUpdateError", "上传失败: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/reader/profile/edit?error=1");
        }
    }

    private void loadBorrowRecords(HttpServletRequest req) {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            req.setAttribute("records", java.util.List.of());
            req.setAttribute("unreturnedCount", 0);
            req.setAttribute("overdueCount", 0);
            return;
        }
        String readerId = String.valueOf(currentUser.getReaderId());
        List<BorrowRecordDetail> records = borrowingService.findByReaderId(readerId);
        int unreturnedCount = borrowingService.getUnreturnedCountByReader(readerId);
        int overdueCount = borrowingService.getOverdueUnreturnedCountByReader(readerId);
        
        req.setAttribute("records", records);
        req.setAttribute("unreturnedCount", unreturnedCount);
        req.setAttribute("overdueCount", overdueCount);
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    private String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return (p == null || p.isBlank()) ? "/profile" : p;
    }
}



