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
        setupResponse(resp);
        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            // 从 Query 参数获取 adminId (模拟，实际应从 Session 获取)
            String adminIdStr = req.getParameter("adminId");
            Integer adminId = (adminIdStr != null) ? Integer.parseInt(adminIdStr) : 1;

            UpsertAnnouncementDto dto = gson.fromJson(reader, UpsertAnnouncementDto.class);
            AnnouncementDto result = service.createAnnouncement(dto, adminId);

            resp.setStatus(201);
            out.print(gson.toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        String pathInfo = req.getPathInfo();

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo == null) {
                resp.setStatus(400); return;
            }

            // 解析 ID
            // pathInfo 可能是 "/123" 或 "/123/takedown"
            String[] parts = pathInfo.split("/");
            // parts[0] 是空字符串, parts[1] 是 ID
            if (parts.length < 2) { resp.setStatus(400); return; }

            Integer id;
            try {
                id = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(400);
                out.print("{\"error\":\"Invalid ID\"}");
                return;
            }

            // 判断是下架还是更新
            if (pathInfo.endsWith("/takedown")) {
                // 下架
                boolean success = service.takedownAnnouncement(id);
                out.print(gson.toJson(success));
            } else {
                // 更新内容
                try (BufferedReader reader = req.getReader()) {
                    UpsertAnnouncementDto dto = gson.fromJson(reader, UpsertAnnouncementDto.class);
                    AnnouncementDto result = service.updateAnnouncement(id, dto);
                    out.print(gson.toJson(result));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, e.getMessage());
        }
    }

    private void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }
}