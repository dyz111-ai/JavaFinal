package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.HandleReportDto;
import com.example.demo0.admin.dto.ReportDetailDto;
import com.example.demo0.admin.dto.ReportDto;
import com.example.demo0.admin.service.ReportService;
import com.google.gson.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.enterprise.inject.spi.CDI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servlet 版本的举报接口，适配前端 /api/admin/reports/* 路径。
 * 支持：
 *  GET  /pending                 获取待处理举报列表
 *  GET  /{id}                    获取举报详情（可选）
 *  POST /add?readerId=xx         新增举报
 *  PUT  /{id}?librarianId=xx     处理举报（approve/reject），请求体为 HandleReportDto JSON
 */
@WebServlet("/api/admin/reports/*")
public class ReportServlet extends HttpServlet {

    // 静态初始化块，类加载时就会执行
    static {
        System.out.println("========================================");
        System.out.println("[ReportServlet] 🔵 类被加载了！");
        System.out.println("[ReportServlet] 类路径: com.example.demo0.admin.controller.ReportServlet");
        System.out.println("========================================");
    }

    private transient ReportService reportService;
    // 配置Gson以支持LocalDateTime序列化
    // Gson默认使用驼峰命名（LOWER_CAMEL_CASE），无需特别设置
    private final Gson gson = new GsonBuilder()
            .serializeNulls() // 序列化null值，避免字段丢失
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    if (src == null) {
                        return JsonNull.INSTANCE;
                    }
                    // 将时间格式化为字符串返回给前端
                    return new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    if (json.isJsonNull()) {
                        return null;
                    }
                    // 如果需要接收时间，按此格式解析
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
            })
            .create();

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("========================================");
        System.out.println("[ReportServlet] ⚡ Servlet初始化开始");
        System.out.println("[ReportServlet] Servlet路径: /api/admin/reports/*");
        try {
            // 通过 CDI 获取业务服务，确保复用已有的 Repository / EntityManager
            this.reportService = CDI.current().select(ReportService.class).get();
            System.out.println("[ReportServlet] ✅ ReportService注入成功");
        } catch (Exception e) {
            System.out.println("[ReportServlet] ❌ ReportService注入失败: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Failed to initialize ReportService", e);
        }
        System.out.println("[ReportServlet] ✅ Servlet初始化完成");
        System.out.println("========================================");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 同时输出到stdout和stderr，确保能看到
        System.out.println("========================================");
        System.err.println("========================================");
        System.out.println("[ReportServlet] 🔵🔵🔵 doGet方法被调用了！");
        System.err.println("[ReportServlet] 🔵🔵🔵 doGet方法被调用了！");
        
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // 例如 /pending 或 /123
        
        System.out.println("[ReportServlet] 收到GET请求，pathInfo: " + pathInfo);
        System.err.println("[ReportServlet] 收到GET请求，pathInfo: " + pathInfo);
        System.out.println("[ReportServlet] 请求URL: " + req.getRequestURL());
        System.err.println("[ReportServlet] 请求URL: " + req.getRequestURL());
        System.out.println("[ReportServlet] Query参数: " + req.getQueryString());
        System.err.println("[ReportServlet] Query参数: " + req.getQueryString());

        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo == null || "/".equals(pathInfo) || "/pending".equals(pathInfo)) {
                System.out.println("[ReportServlet] 开始调用 getPendingReports()");
                long startTime = System.currentTimeMillis();
                
                List<ReportDetailDto> reports = reportService.getPendingReports();
                
                long endTime = System.currentTimeMillis();
                System.out.println("[ReportServlet] getPendingReports() 返回结果数量: " + reports.size());
                System.out.println("[ReportServlet] 查询耗时: " + (endTime - startTime) + "ms");
                
                // 打印前3条记录的详细信息
                if (!reports.isEmpty()) {
                    System.out.println("[ReportServlet] 前3条记录详情:");
                    for (int i = 0; i < Math.min(3, reports.size()); i++) {
                        ReportDetailDto r = reports.get(i);
                        System.out.println("  [" + i + "] ReportID: " + r.getReportId() + 
                                         ", ReportTime: " + r.getReportTime() +
                                         ", CommentTime: " + r.getCommentTime() +
                                         ", BookTitle: " + r.getBookTitle() +
                                         ", ReporterNickname: " + r.getReporterNickname());
                    }
                }
                
                String jsonResult = gson.toJson(reports);
                System.out.println("[ReportServlet] JSON响应长度: " + jsonResult.length() + " 字符");
                System.out.println("[ReportServlet] 返回JSON前500字符: " + (jsonResult.length() > 500 ? jsonResult.substring(0, 500) + "..." : jsonResult));
                
                out.print(jsonResult);
                System.out.println("[ReportServlet] ✅ 响应已发送，状态码: 200");
                System.out.println("========================================");
                return;
            }

            // GET /{id}  获取单条举报详情（可选）
            String[] parts = pathInfo.split("/");
            if (parts.length >= 2) {
                Integer id = Integer.parseInt(parts[1]);
                ReportDetailDto dto = reportService.getReportById(id);
                if (dto == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{}");
                    return;
                }
                out.print(gson.toJson(dto));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Unsupported path\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo();

        // 只支持 /add
        if (pathInfo == null || !pathInfo.equals("/add")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Invalid path\"}");
            return;
        }

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            ReportDto dto = gson.fromJson(reader, ReportDto.class);
            // 从 query 参数补充 readerId
            String readerIdStr = req.getParameter("readerId");
            if (readerIdStr != null && !readerIdStr.isEmpty()) {
                dto.setReaderId(Integer.parseInt(readerIdStr));
            }
            reportService.addReport(dto);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"message\":\"举报添加成功\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String pathInfo = req.getPathInfo(); // /{id}
        if (pathInfo == null || pathInfo.split("/").length < 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\":\"Missing report id\"}");
            return;
        }

        try (BufferedReader reader = req.getReader(); PrintWriter out = resp.getWriter()) {
            String[] parts = pathInfo.split("/");
            Integer id = Integer.parseInt(parts[1]);

            HandleReportDto dto = gson.fromJson(reader, HandleReportDto.class);
            if (dto == null) dto = new HandleReportDto();
            dto.setReportId(id);

            // 补充管理员ID
            String librarianIdStr = req.getParameter("librarianId");
            if (dto.getLibrarianId() == null && librarianIdStr != null && !librarianIdStr.isEmpty()) {
                dto.setLibrarianId(Integer.parseInt(librarianIdStr));
            }

            System.out.println("[ReportServlet] 处理举报请求:");
            System.out.println("  ReportID: " + dto.getReportId());
            System.out.println("  Action: " + dto.getAction());
            System.out.println("  LibrarianID: " + dto.getLibrarianId());
            System.out.println("  CommentID: " + dto.getCommentId());
            System.out.println("  BanUser: " + dto.isBanUser());

            // handleReport 成功时返回 true，失败时抛出异常
            boolean result = reportService.handleReport(dto);
            if (result) {
                // 成功时返回 204 No Content
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                System.out.println("[ReportServlet] ✅ 举报处理成功");
                // 204 状态码不应该有响应体
            } else {
                // 如果返回 false（理论上不应该发生，因为失败会抛异常）
                System.out.println("[ReportServlet] ❌ 举报处理返回false");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"处理失败\"}");
            }
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"" + e.getMessage().replace("\"", "\\\"").replace("\n", " ") + "\"}");
            }
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"" + e.getMessage().replace("\"", "\\\"").replace("\n", " ") + "\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"error\":\"处理失败: " + e.getMessage().replace("\"", "\\\"").replace("\n", " ") + "\"}");
            }
        }
    }
}
