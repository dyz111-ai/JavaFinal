package com.example.demo0.reader.controller;

import com.example.demo0.reader.model.BorrowRecordDetail;
import com.example.demo0.reader.service.BorrowingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(urlPatterns = {"/reader/*"})
public class ReaderController extends HttpServlet {

    private final BorrowingService borrowingService = new BorrowingService();

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

    private void loadBorrowRecords(HttpServletRequest req) {
        // readerId固定为1
        Integer readerId = 1;
        List<BorrowRecordDetail> records = borrowingService.findByReaderId(String.valueOf(readerId));
        int unreturnedCount = borrowingService.getUnreturnedCountByReader(String.valueOf(readerId));
        int overdueCount = borrowingService.getOverdueUnreturnedCountByReader(String.valueOf(readerId));
        
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



