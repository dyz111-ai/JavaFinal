package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.CategoryDto;
import com.example.demo0.admin.dto.CategoryRequest;
import com.example.demo0.admin.service.CategoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/admin/category/*")
public class CategoryApiController extends HttpServlet {

    private final CategoryService service = new CategoryService();
    // 使用默认命名策略，便于前端以小写 camelCase 发送数据；返回的字段名同样为小写
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[CategoryApiController] ========== 收到GET请求 ==========");
        setupResponse(resp);
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo == null || pathInfo.equals("/") || "/tree".equals(pathInfo)) {
                // 获取分类树
                System.out.println("[CategoryApiController] 获取分类树");
                List<CategoryDto> tree = service.getCategoryTree();
                System.out.println("[CategoryApiController] 分类树大小: " + (tree != null ? tree.size() : 0));
                
                // 确保返回的是数组格式
                if (tree == null) {
                    tree = new java.util.ArrayList<>();
                }
                
                String json = gson.toJson(tree);
                System.out.println("[CategoryApiController] 返回JSON长度: " + json.length());
                System.out.println("[CategoryApiController] 返回JSON前200字符: " + (json.length() > 200 ? json.substring(0, 200) : json));
                System.out.println("[CategoryApiController] JSON是否以[开头: " + json.startsWith("["));
                // 若为空数组给出提示
                if (tree.isEmpty()) {
                    System.out.println("[CategoryApiController] ⚠️ 分类树为空（返回 []）");
                }
                
                out.print(json);
                out.flush();
                return;
            } else if (pathInfo.startsWith("/") && pathInfo.length() > 1) {
                // 获取单个分类 /{id}
                String categoryId = pathInfo.substring(1);
                System.out.println("[CategoryApiController] 获取分类: " + categoryId);
                CategoryDto dto = service.getCategoryById(categoryId);
                if (dto != null) {
                    out.print(gson.toJson(dto));
                } else {
                    resp.setStatus(404);
                    out.print("{\"error\":\"分类不存在\"}");
                }
            } else {
                resp.setStatus(400);
                out.print("{\"error\":\"无效的请求路径\"}");
            }
        } catch (Exception e) {
            System.err.println("[CategoryApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
        System.out.println("[CategoryApiController] ========== GET请求处理完成 ==========");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[CategoryApiController] ========== 收到POST请求 ==========");
        setupResponse(resp);

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            System.out.println("[CategoryApiController] 请求体: " + json.toString());

            CategoryRequest request = gson.fromJson(json.toString(), CategoryRequest.class);
            
            if (request == null || request.getCategoryId() == null || request.getCategoryName() == null) {
                System.err.println("[CategoryApiController] ❌ 请求数据不完整: " + json);
                resp.setStatus(400);
                out.print("{\"error\":\"请求数据不完整\"}");
                return;
            }

            boolean success = service.addCategory(
                request.getCategoryId(),
                request.getCategoryName(),
                request.getParentCategoryId()
            );

            if (success) {
                out.print("{\"success\":true,\"message\":\"分类添加成功\"}");
            } else {
                System.err.println("[CategoryApiController] ❌ 分类添加失败，可能原因：ID重复或父分类不存在");
                resp.setStatus(400);
                out.print("{\"error\":\"分类添加失败\"}");
            }
        } catch (Exception e) {
            System.err.println("[CategoryApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
        System.out.println("[CategoryApiController] ========== POST请求处理完成 ==========");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[CategoryApiController] ========== 收到PUT请求 ==========");
        setupResponse(resp);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"缺少分类ID\"}");
            return;
        }

        String categoryId = pathInfo.substring(1);

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            System.out.println("[CategoryApiController] 请求体: " + json.toString());

            CategoryRequest request = gson.fromJson(json.toString(), CategoryRequest.class);
            
            if (request == null || request.getCategoryName() == null) {
                resp.setStatus(400);
                out.print("{\"error\":\"请求数据不完整\"}");
                return;
            }

            boolean success = service.updateCategory(
                categoryId,
                request.getCategoryName(),
                request.getParentCategoryId()
            );

            if (success) {
                out.print("{\"success\":true,\"message\":\"分类更新成功\"}");
            } else {
                resp.setStatus(400);
                out.print("{\"error\":\"分类更新失败\"}");
            }
        } catch (Exception e) {
            System.err.println("[CategoryApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
        System.out.println("[CategoryApiController] ========== PUT请求处理完成 ==========");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[CategoryApiController] ========== 收到DELETE请求 ==========");
        setupResponse(resp);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(400);
            resp.getWriter().print("{\"error\":\"缺少分类ID\"}");
            return;
        }

        String categoryId = pathInfo.substring(1);
        System.out.println("[CategoryApiController] 删除分类: " + categoryId);

        try (PrintWriter out = resp.getWriter()) {
            boolean success = service.deleteCategory(categoryId);

            if (success) {
                out.print("{\"success\":true,\"message\":\"分类删除成功\"}");
            } else {
                resp.setStatus(400);
                out.print("{\"error\":\"分类删除失败，可能该分类下有子分类或关联图书\"}");
            }
        } catch (Exception e) {
            System.err.println("[CategoryApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
        System.out.println("[CategoryApiController] ========== DELETE请求处理完成 ==========");
    }

    private void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }
}

