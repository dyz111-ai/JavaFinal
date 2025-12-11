<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录 - 阅享图书馆</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <style>
        body {
            margin: 0; padding: 0; min-height: 100vh;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background: linear-gradient(135deg, #f0f7ff 0%, #e0e7ff 100%);
            display: flex; align-items: center; justify-content: center;
        }
        .auth-container { width: 100%; max-width: 400px; padding: 20px; }
        .auth-card {
            background: white; padding: 40px; border-radius: 16px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05); text-align: center;
            transition: transform 0.3s ease;
        }
        .auth-card:hover { transform: translateY(-5px); box-shadow: 0 15px 30px rgba(0, 0, 0, 0.08); }

        /* 切换 Tab 样式 */
        .login-tabs {
            display: flex; background: #f3f4f6; border-radius: 8px; padding: 4px;
            margin-bottom: 24px;
        }
        .tab-btn {
            flex: 1; padding: 10px; border: none; background: transparent;
            border-radius: 6px; font-size: 14px; font-weight: 600; color: #6b7280;
            cursor: pointer; transition: all 0.2s;
        }
        .tab-btn.active { background: white; color: #2563eb; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }

        .brand-logo {
            width: 48px; height: 48px; background: #eff6ff; color: #2563eb;
            border-radius: 12px; display: flex; align-items: center; justify-content: center;
            margin: 0 auto 20px;
        }
        .auth-title { font-size: 24px; font-weight: 700; color: #1f2937; margin: 0 0 8px; }
        .auth-subtitle { color: #6b7280; font-size: 14px; margin-bottom: 30px; }
        .form-group { margin-bottom: 20px; text-align: left; }
        .form-label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }
        .form-input {
            width: 100%; padding: 12px 16px; border: 1px solid #d1d5db;
            border-radius: 8px; font-size: 14px; transition: all 0.2s;
            box-sizing: border-box; outline: none;
        }
        .form-input:focus { border-color: #2563eb; box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1); }
        .btn-submit {
            width: 100%; padding: 12px; background-color: #2563eb; color: white;
            border: none; border-radius: 8px; font-size: 15px; font-weight: 600;
            cursor: pointer; transition: background-color 0.2s; margin-top: 10px;
        }
        .btn-submit:hover { background-color: #1d4ed8; }
        .auth-footer { margin-top: 24px; font-size: 14px; color: #6b7280; }
        .auth-link { color: #2563eb; text-decoration: none; font-weight: 500; }
        .auth-link:hover { text-decoration: underline; }
        .alert { padding: 10px 14px; border-radius: 6px; font-size: 13px; margin-bottom: 20px; display: flex; align-items: center; gap: 8px; }
        .alert-error { background-color: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
        .alert-success { background-color: #f0fdf4; color: #16a34a; border: 1px solid #bbf7d0; }

        .hidden { display: none; }
    </style>
</head>
<body>

<div class="auth-container">
    <div class="auth-card">
        <div class="brand-logo">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
        </div>

        <h1 class="auth-title">欢迎回来</h1>
        <p class="auth-subtitle">请输入您的账号信息以继续</p>

        <%-- 身份切换 Tab --%>
        <div class="login-tabs">
            <button type="button" class="tab-btn active" onclick="switchType('reader')" id="tab-reader">我是读者</button>
            <button type="button" class="tab-btn" onclick="switchType('admin')" id="tab-admin">管理员</button>
        </div>

        <%-- 提示信息区域 --%>
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <span>${error}</span>
            </div>
        </c:if>
        <c:if test="${param.msg == 'registered'}">
            <div class="alert alert-success">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                <span>注册成功，请登录！</span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login" method="post" id="loginForm">
            <%-- 隐藏字段，用于告诉后端是读者登录还是管理员登录 --%>
            <input type="hidden" name="loginType" id="loginType" value="reader">

            <div class="form-group">
                <label class="form-label" id="account-label">账号 / 学号</label>
                <%-- 注意：name 默认为 username，切换到管理员时 JS 会改为 staffNo --%>
                <input type="text" name="username" id="account-input" class="form-input" placeholder="请输入您的账号" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label">密码</label>
                <input type="password" name="password" class="form-input" placeholder="请输入您的密码" required>
            </div>

            <button type="submit" class="btn-submit">立即登录</button>
        </form>

        <div class="auth-footer" id="reader-footer">
            还没有账号？ <a href="${pageContext.request.contextPath}/auth/register" class="auth-link">免费注册</a>
            <br><br>
            <a href="${pageContext.request.contextPath}/home" style="color: #9ca3af; text-decoration: none; font-size: 12px;">← 返回首页</a>
        </div>

        <div class="auth-footer hidden" id="admin-footer">
            <a href="${pageContext.request.contextPath}/home" style="color: #9ca3af; text-decoration: none; font-size: 12px;">← 返回首页</a>
        </div>
    </div>
</div>

<script>
    function switchType(type) {
        const loginTypeInput = document.getElementById('loginType');
        const accountLabel = document.getElementById('account-label');
        const accountInput = document.getElementById('account-input');
        const tabReader = document.getElementById('tab-reader');
        const tabAdmin = document.getElementById('tab-admin');
        const readerFooter = document.getElementById('reader-footer');
        const adminFooter = document.getElementById('admin-footer');

        if (type === 'admin') {
            // 切换 UI 状态到管理员
            tabAdmin.classList.add('active');
            tabReader.classList.remove('active');

            // 修改表单参数
            loginTypeInput.value = 'admin';
            accountLabel.textContent = '工号';
            accountInput.placeholder = '请输入管理员账号';
            accountInput.name = 'staffNo'; // 修改提交的参数名

            // 隐藏注册链接
            readerFooter.classList.add('hidden');
            adminFooter.classList.remove('hidden');
        } else {
            // 切换 UI 状态到读者
            tabReader.classList.add('active');
            tabAdmin.classList.remove('active');

            // 修改表单参数
            loginTypeInput.value = 'reader';
            accountLabel.textContent = '账号 / 学号';
            accountInput.placeholder = '请输入您的账号';
            accountInput.name = 'username'; // 恢复参数名

            // 显示注册链接
            readerFooter.classList.remove('hidden');
            adminFooter.classList.add('hidden');
        }
    }
</script>

</body>
</html>