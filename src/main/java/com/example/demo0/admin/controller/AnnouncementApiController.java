package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.AnnouncementDto;
import com.example.demo0.admin.dto.UpsertAnnouncementDto;
import com.example.demo0.admin.service.AnnouncementService;
import com.google.gson.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// 映射路径 /api/admin/announcements/* 匹配 JSP 中的 axios 请求
@WebServlet("/api/admin/announcements/*")
public class AnnouncementApiController extends HttpServlet {

    private final AnnouncementService service = new AnnouncementService();

    // 修复点：直接在这里定义日期格式化逻辑，不再引用外部文件
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    // 将时间格式化为字符串返回给前端
                    return new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    // 如果需要接收时间，按此格式解析
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            })
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取列表
                List<AnnouncementDto> list = service.getAllAnnouncements();
                out.print(gson.toJson(list));
            } else {
                // 获取详情 /{id}
                String idStr = pathInfo.substring(1);
                try {
                    Integer id = Integer.parseInt(idStr);
                    AnnouncementDto dto = service.getAnnouncementById(id);
                    if (dto != null) {
                        out.print(gson.toJson(dto));
                    } else {
                        resp.setStatus(404);
                        out.print("{}");
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Invalid ID format\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[AnnouncementApiController] ========== 收到创建公告请求 ==========");
        setupResponse(resp);
        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            // 从 Query 参数获取 adminId (模拟，实际应从 Session 获取)
            String adminIdStr = req.getParameter("adminId");
            Integer adminId = (adminIdStr != null) ? Integer.parseInt(adminIdStr) : 1;
            System.out.println("[AnnouncementApiController] AdminID: " + adminId);

            // 读取请求体
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            String jsonStr = requestBody.toString();
            System.out.println("[AnnouncementApiController] 请求体JSON: " + jsonStr);

            UpsertAnnouncementDto dto = gson.fromJson(jsonStr, UpsertAnnouncementDto.class);
            System.out.println("[AnnouncementApiController] 解析后的DTO:");
            System.out.println("  Title: " + (dto != null ? dto.getTitle() : "null"));
            System.out.println("  Content: " + (dto != null && dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
            System.out.println("  TargetGroup: " + (dto != null ? dto.getTargetGroup() : "null"));

            if (dto == null) {
                System.out.println("[AnnouncementApiController] ❌ DTO为null");
                resp.sendError(400, "Invalid request body");
                return;
            }

            System.out.println("[AnnouncementApiController] 调用 service.createAnnouncement()...");
            AnnouncementDto result = service.createAnnouncement(dto, adminId);
            
            if (result != null) {
                System.out.println("[AnnouncementApiController] ✅ 创建成功，返回结果:");
                System.out.println("  AnnouncementID: " + result.getAnnouncementId());
                System.out.println("  Title: " + result.getTitle());
                resp.setStatus(201);
                String jsonResult = gson.toJson(result);
                System.out.println("[AnnouncementApiController] 返回JSON: " + jsonResult);
                out.print(jsonResult);
            } else {
                System.out.println("[AnnouncementApiController] ❌ 创建失败，返回null");
                resp.sendError(500, "创建公告失败，返回null");
            }
        } catch (Exception e) {
            System.out.println("[AnnouncementApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
        System.out.println("[AnnouncementApiController] ========== 处理完成 ==========");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[AnnouncementApiController] ========== 收到更新公告请求 ==========");
        setupResponse(resp);
        String pathInfo = req.getPathInfo();
        System.out.println("[AnnouncementApiController] PathInfo: " + pathInfo);

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo == null) {
                System.out.println("[AnnouncementApiController] ❌ PathInfo为null");
                resp.setStatus(400); 
                return;
            }

            // 解析 ID
            // pathInfo 可能是 "/123" 或 "/123/takedown"
            String[] parts = pathInfo.split("/");
            // parts[0] 是空字符串, parts[1] 是 ID
            if (parts.length < 2) { 
                System.out.println("[AnnouncementApiController] ❌ PathInfo格式错误");
                resp.setStatus(400); 
                return; 
            }

            Integer id;
            try {
                id = Integer.parseInt(parts[1]);
                System.out.println("[AnnouncementApiController] AnnouncementID: " + id);
            } catch (NumberFormatException e) {
                System.out.println("[AnnouncementApiController] ❌ ID格式错误: " + parts[1]);
                resp.setStatus(400);
                out.print("{\"error\":\"Invalid ID\"}");
                return;
            }

            // 判断是下架还是更新
            if (pathInfo.endsWith("/takedown")) {
                // 下架
                System.out.println("[AnnouncementApiController] 执行下架操作");
                boolean success = service.takedownAnnouncement(id);
                out.print(gson.toJson(success));
            } else {
                // 更新内容
                System.out.println("[AnnouncementApiController] 执行更新操作");
                
                // 读取请求体
                StringBuilder requestBody = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestBody.append(line);
                    }
                }
                String jsonStr = requestBody.toString();
                System.out.println("[AnnouncementApiController] 请求体JSON: " + jsonStr);
                
                UpsertAnnouncementDto dto = gson.fromJson(jsonStr, UpsertAnnouncementDto.class);
                System.out.println("[AnnouncementApiController] 解析后的DTO:");
                System.out.println("  Title: " + (dto != null ? dto.getTitle() : "null"));
                System.out.println("  Content: " + (dto != null && dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
                System.out.println("  TargetGroup: " + (dto != null ? dto.getTargetGroup() : "null"));
                
                if (dto == null) {
                    System.out.println("[AnnouncementApiController] ❌ DTO为null");
                    resp.sendError(400, "Invalid request body");
                    return;
                }
                
                System.out.println("[AnnouncementApiController] 调用 service.updateAnnouncement()...");
                AnnouncementDto result = service.updateAnnouncement(id, dto);
                
                if (result != null) {
                    System.out.println("[AnnouncementApiController] ✅ 更新成功，返回结果:");
                    System.out.println("  AnnouncementID: " + result.getAnnouncementId());
                    System.out.println("  Title: " + result.getTitle());
                    String jsonResult = gson.toJson(result);
                    System.out.println("[AnnouncementApiController] 返回JSON: " + jsonResult);
                    out.print(jsonResult);
                } else {
                    System.out.println("[AnnouncementApiController] ❌ 更新失败，返回null");
                    resp.sendError(500, "更新公告失败，返回null");
                }
            }
        } catch (Exception e) {
            System.out.println("[AnnouncementApiController] ❌ 异常: " + e.getMessage());
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
        System.out.println("[AnnouncementApiController] ========== 处理完成 ==========");
    }

    private void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }
}