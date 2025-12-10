<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.demo0.auth.model.Reader" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>个人中心 - 图书馆系统</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <style>
        body { background-color: #f3f4f6; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }
        .container { max-width: 800px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }

        .profile-header { background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%); padding: 40px; color: white; display: flex; align-items: center; gap: 20px; }
        .avatar { width: 80px; height: 80px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: bold; border: 2px solid rgba(255,255,255,0.5); }
        .info h1 { margin: 0; font-size: 24px; }
        .info p { margin: 5px 0 0; opacity: 0.9; }

        .profile-body { padding: 30px; }
        .section-title { font-size: 18px; font-weight: 600; color: #111827; margin-bottom: 20px; border-bottom: 1px solid #e5e7eb; padding-bottom: 10px; }

        .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
        .info-item label { display: block; font-size: 13px; color: #6b7280; margin-bottom: 4px; }
        .info-item div { font-size: 16px; color: #111827; font-weight: 500; }

        .actions { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e5e7eb; display: flex; gap: 10px; }
        .btn { padding: 10px 20px; border-radius: 6px; text-decoration: none; font-size: 14px; transition: background 0.2s; }
        .btn-primary { background: #2563eb; color: white; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-outline { border: 1px solid #d1d5db; color: #374151; }
        .btn-outline:hover { background: #f9fafb; }
        .status-ok { color:#059669; }
        .status-bad { color:#dc2626; }
    </style>
</head>
<body>

<div class="container">
    <c:if test="${param.msg == 'updated'}">
        <div style="background:#f0fdf4; color:#16a34a; padding:12px 20px; font-size:14px; border-bottom:1px solid #bbf7d0;">
            ✅ 资料修改成功！
        </div>
    </c:if>
    <%-- 获取当前用户 --%>
    <%
        Reader user = (Reader) session.getAttribute("currentUser");
        if(user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }
    %>

    <div class="profile-header">
        <div class="avatar">
            <%
                String avatar = user.getAvatar();
                if (avatar != null && !avatar.isBlank()) {
            %>
                <img src="<%= avatar %>" alt="avatar" style="width:80px;height:80px;border-radius:50%;object-fit:cover;" />
            <%
                } else {
            %>
                <%= user.getNickname().substring(0, 1).toUpperCase() %>
            <%
                }
            %>
        </div>
        <div class="info">
            <h1><%= user.getNickname() %></h1>
            <p>ID: <%= user.getUsername() %></p>
        </div>
    </div>

    <div class="profile-body">
        <div class="section-title">基本信息</div>
        <%
            String statusColor = "正常".equals(user.getAccountStatus()) ? "#059669" : "#dc2626";
        %>
        <div class="info-grid">
            <div class="info-item">
                <label>用户名</label>
                <div><%= user.getUsername() %></div>
            </div>
            <div class="info-item">
                <label>真实姓名</label>
                <div><%= user.getFullname() == null ? "未填写" : user.getFullname() %></div>
            </div>
            <div class="info-item">
                <label>账户状态</label>
                <div>
                    <span class="<%= "正常".equals(user.getAccountStatus()) ? "status-ok" : "status-bad" %>">
                        <%= user.getAccountStatus() %>
                    </span>
                </div>
            </div>
            <div class="info-item">
                <label>信用积分</label>
                <div style="color: #d97706; font-weight: bold;"><%= user.getCreditScore() %> 分</div>
            </div>
            <div class="info-item">
                <label>用户权限</label>
                <div><%= user.getPermission() %></div>
            </div>
        </div>

        <div class="actions">
            <a href="<%=request.getContextPath()%>/home" class="btn btn-outline">返回首页</a>
            <a href="<%=request.getContextPath()%>/reader/booklists" class="btn btn-outline">个性化推荐</a>
            <%-- 预留编辑功能 --%>
            <a href="<%=request.getContextPath()%>/reader/profile/edit" class="btn btn-primary">编辑资料</a>
        </div>
    </div>
</div>

</body>
</html>