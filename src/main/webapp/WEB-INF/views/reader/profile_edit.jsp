<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.demo0.auth.model.Reader" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>编辑资料 - 图书馆系统</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/main.css" />
    <style>
        body { background-color: #f3f4f6; display: flex; justify-content: center; padding-top: 50px; }
        .edit-card { background: white; width: 100%; max-width: 500px; padding: 30px; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
        .form-group { margin-bottom: 20px; }
        .form-label { display: block; margin-bottom: 8px; font-weight: 500; color: #374151; }
        .form-input { width: 100%; padding: 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; }
        .form-input:read-only { background-color: #f9fafb; color: #6b7280; cursor: not-allowed; }
        .btn-group { display: flex; gap: 10px; margin-top: 30px; }
        .btn-save { flex: 2; background-color: #2563eb; color: white; padding: 10px; border-radius: 6px; border: none; cursor: pointer; font-size: 16px; }
        .btn-cancel { flex: 1; background-color: white; border: 1px solid #d1d5db; color: #374151; padding: 10px; border-radius: 6px; text-align: center; cursor: pointer; text-decoration: none; display: flex; align-items: center; justify-content: center;}
    </style>
</head>
<body>

<div class="edit-card">
    <h2 style="margin-top: 0; margin-bottom: 24px; color: #111827;">编辑个人资料</h2>

    <c:if test="${not empty error}">
        <div style="background:#fef2f2; color:#dc2626; padding:10px; border-radius:6px; margin-bottom:20px; font-size:14px;">
                ${error}
        </div>
    </c:if>

    <form action="<%=request.getContextPath()%>/reader/profile/edit" method="post">
        <div class="form-group">
            <label class="form-label">账号 (不可修改)</label>
            <input type="text" class="form-input" value="${sessionScope.currentUser.username}" readonly>
        </div>

        <div class="form-group">
            <label class="form-label">真实姓名</label>
            <input type="text" name="fullname" class="form-input" value="${sessionScope.currentUser.fullname}" placeholder="请输入真实姓名">
        </div>

        <div class="form-group">
            <label class="form-label">昵称</label>
            <input type="text" name="nickname" class="form-input" value="${sessionScope.currentUser.nickname}" required placeholder="请输入昵称">
        </div>

        <div class="btn-group">
            <a href="<%=request.getContextPath()%>/reader/profile" class="btn-cancel">取消</a>
            <button type="submit" class="btn-save">保存修改</button>
        </div>
    </form>
</div>

</body>
</html>