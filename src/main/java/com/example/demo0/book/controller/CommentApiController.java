package com.example.demo0.book.controller;

import com.example.demo0.book.model.CommentRecord;
import com.example.demo0.book.service.CommentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 与原项目风格对齐的评论接口：
 * - GET /comment/search?ISBN=... -> JSON 列表
 * - POST /comment/add (表单) -> 写死 readerId=1，返回 JSON；若携带 redirect 参数则重定向
 */
@WebServlet(urlPatterns = {"/comment/search", "/comment/add", "/comment/report"})
public class CommentApiController extends HttpServlet {

    private final CommentService service = new CommentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/comment/search".equals(path)) {
            handleSearch(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/comment/add".equals(path)) {
            handleAdd(req, resp);
            return;
        } else if ("/comment/report".equals(path)) {
            handleReport(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String isbn = firstNonBlank(req.getParameter("ISBN"), req.getParameter("isbn"));
        int limit = 100;
        try { if (req.getParameter("limit") != null) limit = Math.min(500, Math.max(1, Integer.parseInt(req.getParameter("limit")))); } catch (Exception ignored) {}
        List<CommentRecord> list = service.findByIsbn(isbn, limit);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("[");
            for (int i = 0; i < list.size(); i++) {
                CommentRecord c = list.get(i);
                if (i > 0) out.print(",");
                out.print("{");
                // 按原项目常用字段命名输出（首字母大写）
                out.print("\"ReaderID\":" + (c.getReaderId()==null?"null":c.getReaderId()));
                out.print(",\"ISBN\":" + quote(c.getIsbn()));
                out.print(",\"Rating\":" + (c.getRating()==null?"null":c.getRating()));
                out.print(",\"ReviewContent\":" + quote(c.getReviewContent()));
                out.print(",\"CreateTime\":" + quote(c.getCreateTime()==null?null:c.getCreateTime().toString()));
                out.print(",\"Status\":" + quote(c.getStatus()));
                out.print("}");
            }
            out.print("]");
        }
    }

    private void handleAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String isbn = firstNonBlank(req.getParameter("ISBN"), req.getParameter("isbn"));
        String ratingStr = firstNonBlank(req.getParameter("rating"), req.getParameter("Rating"));
        String content = firstNonBlank(req.getParameter("reviewcontent"), req.getParameter("ReviewContent"));
        Short rating = null;
        try { if (ratingStr != null) rating = Short.parseShort(ratingStr.trim()); } catch (Exception ignored) {}
        // 读者 ID 按要求写死为 1
        int affected = service.addComment(1L, isbn, rating, content);

        String redirect = req.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            String target = redirect;
            if (!target.startsWith("/") && !target.startsWith("http")) {
                target = "/" + target;
            }
            resp.sendRedirect(req.getContextPath() + target);
            return;
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"Message\":" + quote(affected>0?"评论添加成功":"评论添加失败") + "}");
        }
    }

    private void handleReport(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("commentId");
        String reason = req.getParameter("reason");
        long commentId = -1;
        try { commentId = Long.parseLong(idStr); } catch (Exception ignored) {}
        // 未做登录，这里按原项目示例固定 readerId=1
        long readerId = 1L;

        boolean ok = service.reportComment(commentId, readerId, reason);

        String redirect = req.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) {
            String target = redirect;
            if (!target.startsWith("/") && !target.startsWith("http")) {
                target = "/" + target;
            }
            String sep = target.contains("?") ? "&" : "?";
            resp.sendRedirect(req.getContextPath() + target + sep + "report=" + (ok ? "1" : "0") + "&reportedId=" + commentId);
            return;
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"success\":" + ok + ",\"message\":" + quote(ok ? "已提交举报" : "举报失败") + "}");
        }
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private String quote(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
