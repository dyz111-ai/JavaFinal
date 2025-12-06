package com.example.demo0.home.controller;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.repository.BookRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/home"})
public class HomeController extends HttpServlet {

    private final BookRepository bookRepository = new BookRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 推荐图书（从内存数据取前几本）
        List<BookInfo> all = bookRepository.search(null);
        List<BookInfo> recommends = all.subList(0, Math.min(4, all.size()));

        // 公告（示例数据）
        List<Map<String, String>> announcements = new ArrayList<>();
        announcements.add(makeAnnouncement("系统维护通知", "本周五 22:00-23:00 进行例行维护，届时系统短暂不可用。"));
        announcements.add(makeAnnouncement("新书上架", "新增计算机经典书籍《Clean Code》《Effective Java》。"));

        // 快速入口
        List<Map<String, String>> quickEntries = new ArrayList<>();
        quickEntries.add(makeEntry("图书搜索", req.getContextPath() + "/book/search"));
        quickEntries.add(makeEntry("查看推荐", req.getContextPath() + "/home#recommend"));
        quickEntries.add(makeEntry("我的借阅", "#")); // 占位

        req.setAttribute("recommends", recommends);
        req.setAttribute("announcements", announcements);
        req.setAttribute("quickEntries", quickEntries);
        req.setAttribute("isLoggedIn", Boolean.TRUE);

        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }

    private Map<String, String> makeAnnouncement(String title, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("title", title);
        m.put("content", content);
        return m;
    }

    private Map<String, String> makeEntry(String name, String href) {
        Map<String, String> m = new HashMap<>();
        m.put("name", name);
        m.put("href", href);
        return m;
    }
}

