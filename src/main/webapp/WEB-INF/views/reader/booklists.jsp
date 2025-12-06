<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <title>我的书单</title>
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Microsoft YaHei",sans-serif;margin:0;padding:20px;background:#f7f7f7;color:#374151}
    .container{max-width:900px;margin:0 auto}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:18px}
    .muted{color:#6b7280}
    a{color:#2563eb;text-decoration:none}
  </style>
</head>
<body>
<div class="container">
  <h2>我的书单</h2>
  <div class="card">
    <p class="muted">占位页面：尚未接入书单数据与操作（创建、收藏、增删书籍等）。</p>
    <p><a href="<%=request.getContextPath()%>/home">返回首页</a></p>
  </div>
</div>
</body>
</html>
