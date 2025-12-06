package com.example.demo0.book.controller;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.service.BookSearchService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(urlPatterns = {"/book/search"})
public class BookSearchController extends HttpServlet {

    private final BookSearchService service = new BookSearchService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String keyword = param(req, "keyword");
        if (keyword == null || keyword.isBlank()) {
            // 兼容前端历史参数 q
            keyword = param(req, "q");
        }
        String category = param(req, "category");

        List<BookInfo> list = (category == null || category.isBlank() || "全部".equals(category))
                ? service.search(keyword)
                : service.searchAndFilter(keyword, category);

        // 类别集合从全部搜索结果抽取，便于切换筛选
        List<String> categories = service.extractCategories(service.search(keyword));

        req.setAttribute("keyword", keyword == null ? "" : keyword);
        req.setAttribute("selectedCategory", (category == null || category.isBlank()) ? "全部" : category);
        req.setAttribute("categories", categories);
        req.setAttribute("books", list);

        req.getRequestDispatcher("/WEB-INF/views/book/book-search.jsp").forward(req, resp);
    }

    private String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }
}
