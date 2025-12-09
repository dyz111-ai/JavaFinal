<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.BookInfo" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <title>图书搜索</title>
    <style>
        body{font-family:system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Microsoft YaHei", sans-serif; margin:0; padding:20px; padding-top:5rem; background:#f7f7f7; color:#374151}
        .container{max-width:1100px; margin:0 auto}
        .search-bar{display:flex; gap:8px; margin:10px 0 18px}
        input[type=text]{flex:1; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px}
        button, .btn{background:#2563eb; color:#fff; border:none; padding:10px 14px; border-radius:8px; cursor:pointer}
        .chips{display:flex; flex-wrap:wrap; gap:8px; margin:10px 0 18px}
        .chip{border:1px solid #e5e7eb; background:#fff; color:#374151; padding:6px 12px; border-radius:999px; text-decoration:none}
        .chip.active{background:#2563eb; border-color:#2563eb; color:#fff}
        .grid{display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:18px}
        .card{background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; display:flex; flex-direction:column; align-items:center;}
        .cover{width:180px; height:auto; display:block}
        .title{font-size:16px; font-weight:600; margin:10px 0 6px; text-align:center}
        .meta{font-size:13px; color:#6b7280; margin:3px 0}
        .actions{margin-top:10px; display:flex; gap:10px}
        .link{color:#2563eb; text-decoration:none}
        .empty{text-align:center; padding:40px 0; color:#6b7280}
    </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
    <h2>图书搜索</h2>
    <%
        String keyword = Objects.toString(request.getAttribute("keyword"), "");
        String selectedCategory = Objects.toString(request.getAttribute("selectedCategory"), "全部");
        List<String> categories = (List<String>) request.getAttribute("categories");
        List<BookInfo> books = (List<BookInfo>) request.getAttribute("books");
        if (categories == null) categories = Collections.emptyList();
        if (books == null) books = Collections.emptyList();
        String encKeyword = java.net.URLEncoder.encode(keyword, java.nio.charset.StandardCharsets.UTF_8);
    %>

    <form class="search-bar" method="get" action="<%=request.getContextPath()%>/book/search">
        <input type="text" name="keyword" placeholder="输入书名 / 作者 / ISBN" value="<%=keyword%>"/>
        <button type="submit">搜索</button>
    </form>

    <!-- 分类占位（后续接真实分类后开启） -->
    <div>
        <div style="display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
            <span style="font-size:12px; color:#6b7280">按分类筛选：</span>
            <a class="chip <%= "全部".equals(selectedCategory) ? "active" : "" %>" href="<%=request.getContextPath()%>/book/search?keyword=<%=encKeyword%>&category=%E5%85%A8%E9%83%A8">全部</a>
            <%
                for (String c : categories) {
                    String href = request.getContextPath()+"/book/search?keyword="+encKeyword+"&category="+java.net.URLEncoder.encode(c, java.nio.charset.StandardCharsets.UTF_8);
            %>
                <a class="chip <%= c.equals(selectedCategory) ? "active" : "" %>" href="<%=href%>"><%=c%></a>
            <%
                }
            %>
        </div>
    </div>

    <%
        if (books.isEmpty()) {
    %>
        <p class="empty">未找到相关图书</p>
    <%
        } else {
    %>
    <div class="grid">
        <%
            for (BookInfo b : books) {
                String rawIsbn = b.getISBN()==null?"":b.getISBN().trim();
                String isbnFile = rawIsbn.replaceAll("[^0-9Xx]", "");
                String cover = request.getContextPath()+"/covers/"+isbnFile+".jpg";
        %>
        <div class="card">
            <img class="cover" src="<%=cover%>" alt="封面" onerror="this.onerror=null;this.src='data:image/svg+xml;charset=UTF-8,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'180\' height=\'260\'%3E%3Crect width=\'100%25\' height=\'100%25\' fill=\'%23e5e7eb\'/%3E%3Ctext x=\'50%25\' y=\'50%25\' dominant-baseline=\'middle\' text-anchor=\'middle\' fill=\'%239ca3af\' font-family=\'Arial\' font-size=\'14\'%3E无封面%3C/text%3E%3C/svg%3E'"/>
            <div class="title"><%= b.getTitle() %></div>
            <div class="meta">作者：<%= b.getAuthor() %></div>
            <div class="meta">ISBN：<%= b.getISBN() %></div>
            <div class="meta">库存：<%= b.getStock()==null?"-":b.getStock() %></div>
            <div class="actions">
                <a class="link" href="<%=request.getContextPath()%>/comment/list?isbn=<%= rawIsbn %>">查看评论</a>
                <a class="link" href="<%=request.getContextPath()%>/book/physical?isbn=<%= java.net.URLEncoder.encode(rawIsbn, java.nio.charset.StandardCharsets.UTF_8) %>">查看实体书</a>

            </div>
        </div>
        <%
            }
        %>
    </div>
    <%
        }
    %>
</div>
</body>
</html>