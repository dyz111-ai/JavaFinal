package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.CreatePurchaseLogDto;
import com.example.demo0.admin.service.PurchaseAnalysisService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/api/admin/purchase-analysis/*")
public class PurchaseAnalysisApiServlet extends HttpServlet {
    
    @Inject
    private PurchaseAnalysisService purchaseAnalysisService;
    
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("[PurchaseAnalysisApiServlet] ========== Servlet初始化 ==========");
        // 配置Gson，处理LocalDateTime
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new com.google.gson.JsonSerializer<LocalDateTime>() {
                    @Override
                    public com.google.gson.JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
                        if (src == null) {
                            return null;
                        }
                        return new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    }
                })
                .serializeNulls()
                .create();
        System.out.println("[PurchaseAnalysisApiServlet] Gson配置完成");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[PurchaseAnalysisApiServlet] ========== 收到GET请求 ==========");
        setupResponse(resp);
        String pathInfo = req.getPathInfo();
        System.out.println("[PurchaseAnalysisApiServlet] PathInfo: " + pathInfo);
        
        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/admin/purchase-analysis - 获取分析数据
                System.out.println("[PurchaseAnalysisApiServlet] 获取分析数据");
                try {
                    var analysis = purchaseAnalysisService.getPurchaseAnalysis();
                    System.out.println("[PurchaseAnalysisApiServlet] ✅ 获取分析数据成功");
                    String json = gson.toJson(analysis);
                    System.out.println("[PurchaseAnalysisApiServlet] 返回JSON长度: " + json.length());
                    out.print(json);
                } catch (Exception e) {
                    System.out.println("[PurchaseAnalysisApiServlet] ❌ 获取分析数据失败: " + e.getMessage());
                    e.printStackTrace();
                    resp.setStatus(500);
                    out.print("{\"error\":\"获取分析数据失败: " + escapeJson(e.getMessage()) + "\"}");
                }
            } else if (pathInfo.equals("/logs")) {
                // GET /api/admin/purchase-analysis/logs - 获取日志列表
                System.out.println("[PurchaseAnalysisApiServlet] 获取日志列表");
                try {
                    var logs = purchaseAnalysisService.getPurchaseLogs();
                    System.out.println("[PurchaseAnalysisApiServlet] ✅ 获取日志列表成功，数量: " + logs.size());
                    String json = gson.toJson(logs);
                    out.print(json);
                } catch (Exception e) {
                    System.out.println("[PurchaseAnalysisApiServlet] ❌ 获取日志列表失败: " + e.getMessage());
                    e.printStackTrace();
                    resp.setStatus(500);
                    out.print("{\"error\":\"获取日志列表失败: " + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                System.out.println("[PurchaseAnalysisApiServlet] ❌ 未知路径: " + pathInfo);
                resp.setStatus(404);
                out.print("{\"error\":\"Not Found\"}");
            }
        }
        System.out.println("[PurchaseAnalysisApiServlet] ========== GET请求处理完成 ==========");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("[PurchaseAnalysisApiServlet] ========== 收到POST请求 ==========");
        setupResponse(resp);
        String pathInfo = req.getPathInfo();
        System.out.println("[PurchaseAnalysisApiServlet] PathInfo: " + pathInfo);
        
        try (PrintWriter out = resp.getWriter()) {
            if (pathInfo != null && pathInfo.equals("/logs")) {
                // POST /api/admin/purchase-analysis/logs - 添加日志
                System.out.println("[PurchaseAnalysisApiServlet] 添加采购日志");
                
                // 读取请求体
                StringBuilder requestBody = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestBody.append(line);
                    }
                }
                String jsonStr = requestBody.toString();
                System.out.println("[PurchaseAnalysisApiServlet] 请求体JSON: " + jsonStr);
                
                CreatePurchaseLogDto dto = gson.fromJson(jsonStr, CreatePurchaseLogDto.class);
                System.out.println("[PurchaseAnalysisApiServlet] 解析后的DTO:");
                System.out.println("  LogText: " + (dto != null ? dto.getLogText() : "null"));
                System.out.println("  AdminId: " + (dto != null ? dto.getAdminId() : "null"));
                
                if (dto == null) {
                    System.out.println("[PurchaseAnalysisApiServlet] ❌ DTO为null");
                    resp.setStatus(400);
                    out.print("{\"error\":\"Invalid request body\"}");
                    return;
                }
                
                if (dto.getLogText() == null || dto.getLogText().trim().isEmpty()) {
                    System.out.println("[PurchaseAnalysisApiServlet] ❌ 日志文本为空");
                    resp.setStatus(400);
                    out.print("{\"error\":\"Log text cannot be empty.\"}");
                    return;
                }
                
                if (dto.getAdminId() == null) {
                    System.out.println("[PurchaseAnalysisApiServlet] ❌ 管理员ID为空");
                    resp.setStatus(400);
                    out.print("{\"error\":\"Admin ID cannot be empty.\"}");
                    return;
                }
                
                try {
                    System.out.println("[PurchaseAnalysisApiServlet] 调用 service.addPurchaseLog()...");
                    purchaseAnalysisService.addPurchaseLog(dto.getLogText(), dto.getAdminId());
                    System.out.println("[PurchaseAnalysisApiServlet] ✅ 添加日志成功");
                    resp.setStatus(201);
                    out.print("{\"success\":true}");
                } catch (Exception e) {
                    System.out.println("[PurchaseAnalysisApiServlet] ❌ 添加日志失败: " + e.getMessage());
                    e.printStackTrace();
                    resp.setStatus(500);
                    out.print("{\"error\":\"添加日志失败: " + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                System.out.println("[PurchaseAnalysisApiServlet] ❌ 未知路径: " + pathInfo);
                resp.setStatus(404);
                out.print("{\"error\":\"Not Found\"}");
            }
        }
        System.out.println("[PurchaseAnalysisApiServlet] ========== POST请求处理完成 ==========");
    }
    
    private void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }
    
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
}

