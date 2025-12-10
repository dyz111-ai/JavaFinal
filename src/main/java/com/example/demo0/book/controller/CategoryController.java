package com.example.demo0.book.controller;

import com.example.demo0.book.model.CategoryNode;
import com.example.demo0.book.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 分类页面控制器
 * 处理分类显示页面的请求（读者查看）
 * 实现层级浏览：点击父分类显示子分类，点击叶子分类显示图书
 */
@WebServlet(urlPatterns = {"/category/display"})
public class CategoryController extends HttpServlet {

    private final CategoryService service = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String currentId = req.getParameter("categoryId");
        List<CategoryNode> displayList;

        if (currentId == null || currentId.isBlank()) {
            // 1. 没有参数，显示所有根分类
            displayList = service.getCategoryTree();
            req.setAttribute("categoryList", displayList);
            req.setAttribute("isRoot", true);
            req.getRequestDispatcher("/WEB-INF/views/book/category-display.jsp").forward(req, resp);
        } else {
            // 2. 有参数，查找对应节点
            CategoryNode node = service.findNodeById(currentId);

            if (node != null && node.getChildren() != null && !node.getChildren().isEmpty()) {
                // 2a. 该节点有子分类 -> 显示子分类列表
                displayList = node.getChildren();
                req.setAttribute("categoryList", displayList);
                req.setAttribute("currentCategory", node);
                req.setAttribute("isRoot", false);

                // 获取路径用于面包屑导航 (例如: 根 > 科学 > 物理)
                List<String> path = service.getCategoryPath(currentId);
                req.setAttribute("categoryPath", path);

                req.getRequestDispatcher("/WEB-INF/views/book/category-display.jsp").forward(req, resp);
            } else {
                // 2b. 该节点没有子分类 (是叶子节点) 或未找到 -> 跳转到图书列表
                String name = (node != null) ? node.getCategoryName() : "未知分类";
                String redirectUrl = req.getContextPath() + "/category/books?categoryId="
                        + URLEncoder.encode(currentId, StandardCharsets.UTF_8)
                        + "&categoryName=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
                resp.sendRedirect(redirectUrl);
            }
        }
    }
}