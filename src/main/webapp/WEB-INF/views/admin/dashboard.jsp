<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员仪表盘 - 图书馆管理系统</title>
    <link href="https://cdn.tailwindcss.com" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
    <style>
        .dashboard-card {
            display: block;
            padding: 2rem;
            border-radius: 0.5rem;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            cursor: pointer;
            transition: all 0.3s;
            transform: translateY(0);
            background-color: rgba(255, 255, 255, 0.7);
            backdrop-filter: blur(4px);
            border: 1px solid #000000;
            text-decoration: none;
        }

        .dashboard-card:hover {
            transform: translateY(-0.5rem);
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            border: 1px solid #000000;
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 700;
            margin-bottom: 0.75rem;
            color: #1f2937;
        }

        .card-description {
            font-size: 0.875rem;
            color: #4b5563;
            font-weight: 500;
        }
    </style>
</head>
<body class="bg-gray-50 min-h-screen">
<jsp:include page="navbar.jsp" />

<div class="container mx-auto px-4 py-8">
    <div class="dashboard-container">
        <h2 class="text-3xl font-extrabold mb-8 text-gray-800" style="text-shadow: 1px 1px 3px rgba(0,0,0,0.1);">
            功能导航
        </h2>

        <div class="grid grid-cols-2 gap-8">
            <a href="<%= request.getContextPath() %>/admin/books" class="dashboard-card border-l-4 border-blue-500 hover:bg-blue-50/60">
                <h3 class="card-title">图书管理</h3>
                <p class="card-description">新增、查询、修改和删除图书信息。</p>
            </a>

            <a href="<%= request.getContextPath() %>/admin/announcements" class="dashboard-card border-l-4 border-rose-500 hover:bg-rose-50/60">
                <h3 class="card-title">公告发布</h3>
                <p class="card-description">发布图书馆最新通知与公告。</p>
            </a>

            <a href="<%= request.getContextPath() %>/admin/purchase-analysis" class="dashboard-card border-l-4 border-indigo-500 hover:bg-indigo-50/60">
                <h3 class="card-title">采购分析</h3>
                <p class="card-description">基于借阅数据分析采购需求。</p>
            </a>

            <a href="<%= request.getContextPath() %>/admin/report-handling" class="dashboard-card border-l-4 border-red-500 hover:bg-red-50/60">
                <h3 class="card-title">举报处理</h3>
                <p class="card-description">处理用户提交的各类举报信息。</p>
            </a>
        </div>
    </div>
</div>
</body>
</html>