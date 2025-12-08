<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册 - 阅享图书馆</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <style>
        body {
            margin: 0;
            padding: 0;
            min-height: 100vh;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%); /* 清新的绿色渐变，区分登录页 */
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .auth-container {
            width: 100%;
            max-width: 480px; /* 注册页稍微宽一点 */
            padding: 20px;
        }

        .auth-card {
            background: white;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
            text-align: center;
            transition: transform 0.3s ease;
        }

        .auth-card:hover {
            transform: translateY(-5px);
        }

        .brand-logo {
            width: 48px;
            height: 48px;
            background: #f0fdf4;
            color: #16a34a;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
        }

        .auth-title {
            font-size: 24px;
            font-weight: 700;
            color: #1f2937;
            margin: 0 0 8px;
        }

        .auth-subtitle {
            color: #6b7280;
            font-size: 14px;
            margin-bottom: 30px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 16px;
        }

        .form-group {
            margin-bottom: 20px;
            text-align: left;
        }

        .form-group.full-width {
            grid-column: span 2;
        }

        .form-label {
            display: block;
            font-size: 13px;
            font-weight: 500;
            color: #374151;
            margin-bottom: 6px;
        }

        .form-input {
            width: 100%;
            padding: 12px 16px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.2s;
            box-sizing: border-box;
            outline: none;
        }

        .form-input:focus {
            border-color: #16a34a; /* 绿色主题 */
            box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.1);
        }

        .btn-submit {
            width: 100%;
            padding: 12px;
            background-color: #16a34a; /* 绿色按钮 */
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.2s;
            margin-top: 10px;
        }

        .btn-submit:hover {
            background-color: #15803d;
        }

        .auth-footer {
            margin-top: 24px;
            font-size: 14px;
            color: #6b7280;
        }

        .auth-link {
            color: #16a34a;
            text-decoration: none;
            font-weight: 500;
        }

        .auth-link:hover {
            text-decoration: underline;
        }

        .alert-error {
            background-color: #fef2f2;
            color: #dc2626;
            border: 1px solid #fecaca;
            padding: 10px 14px;
            border-radius: 6px;
            font-size: 13px;
            margin-bottom: 20px;
            text-align: left;
            display: flex;
            align-items: center;
            gap: 8px;
        }
    </style>
</head>
<body>

<div class="auth-container">
    <div class="auth-card">
        <div class="brand-logo">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
        </div>

        <h1 class="auth-title">创建新账号</h1>
        <p class="auth-subtitle">加入我们，开启阅读之旅</p>

        <c:if test="${not empty error}">
            <div class="alert-error">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <span>${error}</span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/register" method="post">
            <div class="form-grid">
                <div class="form-group full-width">
                    <label class="form-label">账号 / 学号 <span style="color:red">*</span></label>
                    <input type="text" name="username" class="form-input" placeholder="设置您的登录账号" required>
                </div>

                <div class="form-group full-width">
                    <label class="form-label">密码 <span style="color:red">*</span></label>
                    <input type="password" name="password" class="form-input" placeholder="设置您的登录密码" required>
                </div>

                <div class="form-group">
                    <label class="form-label">真实姓名</label>
                    <input type="text" name="fullname" class="form-input" placeholder="您的姓名">
                </div>

                <div class="form-group">
                    <label class="form-label">昵称</label>
                    <input type="text" name="nickname" class="form-input" placeholder="显示昵称">
                </div>
            </div>

            <button type="submit" class="btn-submit">立即注册</button>
        </form>

        <div class="auth-footer">
            已有账号？ <a href="${pageContext.request.contextPath}/auth/login" class="auth-link">直接登录</a>
            <br><br>
            <a href="${pageContext.request.contextPath}/home" style="color: #9ca3af; text-decoration: none; font-size: 12px;">← 返回首页</a>
        </div>
    </div>
</div>

</body>
</html>