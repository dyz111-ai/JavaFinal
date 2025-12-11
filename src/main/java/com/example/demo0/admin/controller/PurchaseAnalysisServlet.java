package com.example.demo0.admin.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "PurchaseAnalysisServlet", urlPatterns = {"/admin/purchase-analysis"})
public class PurchaseAnalysisServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 直接转发到采购分析页面
        request.getRequestDispatcher("/WEB-INF/views/admin/purchase-analysis.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 对于POST请求，也转发到同一个页面
        doGet(request, response);
    }
}