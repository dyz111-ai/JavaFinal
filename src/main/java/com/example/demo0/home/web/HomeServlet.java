package com.example.demo0.home.web;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.repository.BookRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private final BookRepository bookRepository = new BookRepository();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // ============================================================
        // 【新增逻辑】权限检查：只有登录用户才能看首页
        // ============================================================
        HttpSession session = req.getSession(false); // 获取当前会话，不自动创建
        Object user = (session != null) ? session.getAttribute("currentUser") : null;

        if (user == null) {
            // 如果没登录，直接重定向到登录页面
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return; // 结束方法，不执行后面加载数据的代码
        }

        // ============================================================
        // 下面是原有的业务逻辑（加载推荐书、公告等），只有登录后才会执行
        // ============================================================

        // 1. 推荐图书（从数据库获取）
        List<BookInfo> all = bookRepository.search(null);
        List<BookInfo> recommends = all.subList(0, Math.min(4, all.size()));

        // 2. 公告（示例数据）
        List<Map<String, String>> announcements = new ArrayList<>();
        announcements.add(makeAnnouncement("系统维护通知", "本周五 22:00-23:00 进行例行维护，届时系统短暂不可用。"));
        announcements.add(makeAnnouncement("新书上架", "新增计算机经典书籍《Clean Code》《Effective Java》。"));

        // 3. 快速入口
        List<Map<String, String>> quickEntries = new ArrayList<>();
        quickEntries.add(makeEntry("图书搜索", req.getContextPath() + "/book/search"));
        quickEntries.add(makeEntry("查看推荐", req.getContextPath() + "/home#recommend"));
        quickEntries.add(makeEntry("我的借阅", req.getContextPath() + "/reader/borrow-records"));

        // 4. 将数据放入请求域，转发给 JSP
        req.setAttribute("recommends", recommends);
        req.setAttribute("announcements", announcements);
        req.setAttribute("quickEntries", quickEntries);
        // 这里不需要再手动设置 isLoggedIn 了，因为能走到这里肯定已登录

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