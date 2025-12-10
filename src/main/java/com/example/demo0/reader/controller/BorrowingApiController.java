package com.example.demo0.reader.controller;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.reader.model.BorrowRecordDetail;
import com.example.demo0.reader.service.BorrowingService;
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
 * 借阅功能REST API控制器
 * 
 * API端点（基于当前登录读者）：
 * - POST /api/borrowing/borrow?barcode=xxx - 借阅图书（当前登录读者）
 * - POST /api/borrowing/return?barcode=xxx - 归还图书（当前登录读者）
 * - GET /api/borrowing/reader - 查询“我”的借阅记录（当前登录读者）
 * - GET /api/borrowing/{id} - 查询单条借阅记录
 * - GET /api/borrowing/unreturned-count/{readerId} - 获取指定读者未归还数量
 * - GET /api/borrowing/overdue-unreturned-count/{readerId} - 获取指定读者逾期未归还数量
 */
@WebServlet(urlPatterns = {"/api/borrowing/*"})
public class BorrowingApiController extends HttpServlet {

    private final BorrowingService service = new BorrowingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            if ("/reader".equals(pathInfo)) {
                // 查询当前登录读者的借阅记录
                handleGetMyRecords(req, resp, out);
            } else if (pathInfo.startsWith("/unreturned-count/")) {
                // 获取未归还数量（路径中指定 readerId）
                String readerId = pathInfo.substring("/unreturned-count/".length());
                handleGetUnreturnedCount(req, resp, out, readerId);
            } else if (pathInfo.startsWith("/overdue-unreturned-count/")) {
                // 获取逾期未归还数量（路径中指定 readerId）
                String readerId = pathInfo.substring("/overdue-unreturned-count/".length());
                handleGetOverdueCount(req, resp, out, readerId);
            } else if (pathInfo.startsWith("/") && pathInfo.length() > 1) {
                // 查询单条借阅记录 /api/borrowing/{id}
                String idStr = pathInfo.substring(1);
                try {
                    Integer id = Integer.parseInt(idStr);
                    handleGetById(req, resp, out, id);
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"message\":\"无效的借阅记录ID\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"message\":\"无效的请求路径\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            if ("/borrow".equals(pathInfo)) {
                // 借阅图书
                handleBorrow(req, resp, out);
            } else if ("/return".equals(pathInfo)) {
                // 归还图书
                handleReturn(req, resp, out);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"message\":\"无效的请求路径\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"message\":\"" + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private void handleGetMyRecords(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"message\":\"未登录或登录状态已失效\"}");
            return;
        }
        String readerId = String.valueOf(currentUser.getReaderId());
        List<BorrowRecordDetail> records = service.findByReaderId(readerId);
        
        out.print("[");
        for (int i = 0; i < records.size(); i++) {
            if (i > 0) out.print(",");
            writeBorrowRecordDetailJson(out, records.get(i));
        }
        out.print("]");
    }

    private void handleGetById(HttpServletRequest req, HttpServletResponse resp, PrintWriter out, Integer id) {
        BorrowRecordDetail record = service.findById(id);
        if (record == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"message\":\"借阅记录不存在\"}");
        } else {
            writeBorrowRecordDetailJson(out, record);
        }
    }

    private void handleGetUnreturnedCount(HttpServletRequest req, HttpServletResponse resp, 
                                          PrintWriter out, String readerId) {
        int count = service.getUnreturnedCountByReader(readerId);
        out.print("{\"count\":" + count + "}");
    }

    private void handleGetOverdueCount(HttpServletRequest req, HttpServletResponse resp, 
                                      PrintWriter out, String readerId) {
        int count = service.getOverdueUnreturnedCountByReader(readerId);
        out.print("{\"count\":" + count + "}");
    }

    private void handleBorrow(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"未登录或登录状态已失效\"}");
            return;
        }
        String readerId = String.valueOf(currentUser.getReaderId());
        String barcode = req.getParameter("barcode");

        if (barcode == null || barcode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"图书条形码不能为空\"}");
            return;
        }

        BorrowingService.BorrowResult result = service.borrowBook(readerId, barcode);
        
        if (result.success) {
            out.print("{");
            out.print("\"success\":true");
            out.print(",\"message\":" + quote(result.message));
            out.print(",\"data\":" + quote(result.data));
            out.print("}");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{");
            out.print("\"success\":false");
            out.print(",\"message\":" + quote(result.message));
            out.print("}");
        }
    }

    private void handleReturn(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"message\":\"未登录或登录状态已失效\"}");
            return;
        }
        String readerId = String.valueOf(currentUser.getReaderId());
        String barcode = req.getParameter("barcode");

        if (barcode == null || barcode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"图书条形码不能为空\"}");
            return;
        }

        BorrowingService.ReturnResult result = service.returnBook(readerId, barcode);

        if (result.success) {
            out.print("{\"success\":true,\"message\":" + quote(result.message) + "}");
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            out.print("{\"success\":false,\"message\":" + quote(result.message) + "}");
        }
    }

    private void writeBorrowRecordDetailJson(PrintWriter out, BorrowRecordDetail record) {
        out.print("{");
        out.print("\"BorrowRecordID\":" + (record.getBorrowRecordId() == null ? "null" : record.getBorrowRecordId()));
        out.print(",\"BookID\":" + quote(record.getBookId()));
        out.print(",\"ISBN\":" + quote(record.getIsbn()));
        out.print(",\"BookTitle\":" + quote(record.getBookTitle()));
        out.print(",\"BookAuthor\":" + quote(record.getBookAuthor()));
        out.print(",\"ReaderID\":" + quote(record.getReaderId()));
        out.print(",\"ReaderName\":" + quote(record.getReaderName()));
        out.print(",\"BorrowTime\":" + quote(record.getBorrowTime() == null ? null : record.getBorrowTime().toString()));
        out.print(",\"ReturnTime\":" + quote(record.getReturnTime() == null ? null : record.getReturnTime().toString()));
        out.print(",\"OverdueFine\":" + (record.getOverdueFine() == null ? "null" : record.getOverdueFine()));
        out.print("}");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String quote(String s) {
        if (s == null) return "null";
        return "\"" + escapeJson(s) + "\"";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}


