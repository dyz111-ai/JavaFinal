<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.demo0.auth.model.Reader" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>编辑资料 - 图书馆系统</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <style>
        body { background-color: #f3f4f6; padding: 20px; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; }
        .container { max-width: 720px; margin: 0 auto; background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); overflow: hidden; }
        .header { background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%); padding: 28px; color: white; }
        .header h1 { margin: 0; font-size: 22px; }
        .body { padding: 28px; }
        .form-grid { display: grid; grid-template-columns: 1fr; gap: 18px; }
        label { display: block; font-size: 14px; color: #374151; margin-bottom: 6px; }
        input[type="text"] { width: 100%; padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 14px; }
        .actions { margin-top: 24px; display: flex; gap: 10px; }
        .btn { padding: 10px 18px; border-radius: 8px; font-size: 14px; text-decoration: none; border: none; cursor: pointer; transition: all 0.2s; }
        .btn-primary { background: #2563eb; color: white; }
        .btn-primary:hover { background: #1d4ed8; }
        .btn-outline { border: 1px solid #d1d5db; color: #374151; background: white; }
        .btn-outline:hover { background: #f9fafb; }
        .error { background:#fef2f2; color:#b91c1c; padding:12px 16px; border-radius: 8px; border:1px solid #fecdd3; margin-bottom:16px; }
        .tip { color:#6b7280; font-size:13px; margin-top:4px; }
    </style>
</head>
<body>
<%
    Reader user = (Reader) session.getAttribute("currentUser");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }
    String err = (String) session.getAttribute("profileUpdateError");
    if (err != null) {
        session.removeAttribute("profileUpdateError");
    }
%>

<div class="container">
    <div class="header">
        <h1>编辑个人资料</h1>
        <p style="margin:8px 0 0; opacity:0.9;">更新你的昵称与真实姓名</p>
    </div>
    <div class="body">
        <% if (err != null && !err.isBlank()) { %>
        <div class="error"><%= err %></div>
        <% } %>
        <form method="post" action="<%=request.getContextPath()%>/reader/profile/edit">
            <div class="form-grid">
                <div>
                    <label for="username">用户名（必填）</label>
                    <input id="username" name="username" type="text" value="<%= user.getUsername() == null ? "" : user.getUsername() %>" required />
                    <div class="tip">登录账号，需唯一</div>
                </div>
                <div>
                    <label for="nickname">昵称（必填）</label>
                    <input id="nickname" name="nickname" type="text" value="<%= user.getNickname() == null ? "" : user.getNickname() %>" required />
                    <div class="tip">用于展示的名字，不能为空</div>
                </div>
                <div>
                    <label for="fullname">真实姓名（可选）</label>
                    <input id="fullname" name="fullname" type="text" value="<%= user.getFullname() == null ? "" : user.getFullname() %>" />
                    <div class="tip">便于馆内识别，可留空</div>
                </div>
                <div>
                    <label for="avatar">头像地址（可选）</label>
                    <input id="avatar" name="avatar" type="text" value="<%= user.getAvatar() == null ? "" : user.getAvatar() %>" />
                    <div class="tip">可填写图片 URL，或下方上传头像</div>
                </div>
            </div>
            <div class="actions">
                <a class="btn btn-outline" href="<%=request.getContextPath()%>/reader/profile">返回</a>
                <button type="submit" class="btn btn-primary">保存</button>
            </div>
        </form>

        <form method="post" action="<%=request.getContextPath()%>/reader/profile/avatar" enctype="multipart/form-data" style="margin-top:18px;">
            <div class="form-grid">
                <div>
                    <label for="avatarFile">上传头像（jpg/jpeg/png，≤1MB）</label>
                    <input id="avatarFile" name="avatarFile" type="file" accept=".jpg,.jpeg,.png" />
                    <div class="tip">上传后将自动保存并更新头像地址</div>
                </div>
            </div>
            <div class="actions">
                <button type="submit" class="btn btn-primary">上传头像</button>
            </div>
        </form>
    </div>
</div>

</body>
</html>

