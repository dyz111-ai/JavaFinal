<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo0.book.model.BookInfo" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <title>图书详情</title>
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Microsoft YaHei",sans-serif;margin:0;padding:20px;background:#f7f7f7;color:#374151}
    .container{max-width:900px;margin:0 auto}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:18px;display:flex;gap:20px}
    .cover{width:220px;height:auto;border-radius:6px}
    .title{font-size:20px;font-weight:700;margin:4px 0 8px}
    .meta{font-size:14px;color:#6b7280;margin:6px 0}
    .actions{margin-top:12px}
    .link{color:#2563eb;text-decoration:none}
  </style>
</head>
<body>
<div class="container">
  <h2>图书详情</h2>
  <%
    BookInfo b = (BookInfo) request.getAttribute("book");
    if (b == null) {
  %>
    <p>未找到该图书。</p>
  <%
    } else {
      String rawIsbn = b.getISBN()==null?"":b.getISBN().trim();
      String isbnFile = rawIsbn.replaceAll("[^0-9Xx]", "");
      String cover = request.getContextPath()+"/covers/"+isbnFile+".jpg";
  %>
  <div class="card">
    <img class="cover" src="<%=cover%>" alt="封面" onerror="this.onerror=null;this.src='data:image/svg+xml;charset=UTF-8,<svg xmlns='http://www.w3.org/2000/svg' width='220' height='300'><rect width='100%' height='100%' fill='%23e5e7eb'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-family='Arial' font-size='14'>无封面</text></svg>'"/>
    <div>
      <div class="title"><%= b.getTitle() %></div>
      <div class="meta">作者：<%= b.getAuthor() %></div>
      <div class="meta">ISBN：<%= b.getISBN() %></div>
      <div class="meta">库存：<%= b.getStock()==null?"-":b.getStock() %></div>
      <div class="actions">
        <a class="link" href="<%=request.getContextPath()%>/comment/list?isbn=<%= rawIsbn %>">查看评论</a>
        &nbsp;|&nbsp;
        <a class="link" href="<%=request.getContextPath()%>/book/search?keyword=<%= java.net.URLEncoder.encode(b.getTitle(), java.nio.charset.StandardCharsets.UTF_8) %>">返回搜索</a>
      </div>
    </div>
  </div>
  <%
    }
  %>
</div>
</body>
</html>
