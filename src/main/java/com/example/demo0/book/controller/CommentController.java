package com.example.demo0.book.controller;

import com.example.demo0.book.model.CommentRecord;
import com.example.demo0.book.service.CommentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/comment/list"})
public class CommentController extends HttpServlet {

    private final CommentService service = new CommentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String isbn = req.getParameter("isbn");
        req.setAttribute("isbn", isbn);
        List<CommentRecord> comments = service.findByIsbn(isbn, 50);
        req.setAttribute("comments", comments);
        req.getRequestDispatcher("/WEB-INF/views/book/comment-list.jsp").forward(req, resp);
    }
}
