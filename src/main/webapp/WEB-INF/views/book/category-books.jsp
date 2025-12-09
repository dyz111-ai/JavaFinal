<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.BookCategoryDetail" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <title>分类图书</title>
    <style>
        body{font-family:system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Microsoft YaHei", sans-serif; margin:0; padding:20px; padding-top:5rem; background:#f7f7f7; color:#374151}
        .container{max-width:1100px; margin:0 auto}
        .page-header{margin-bottom:24px; text-align:center}
        .page-header h1{font-size:24px; font-weight:700; margin:0 0 8px}
        .page-header p{color:#6b7280; margin:0}
        .grid{display:grid; grid-template-columns:repeat(auto-fill, minmax(240px, 1fr)); gap:18px}
        .card{background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:16px; display:flex; flex-direction:column; align-items:center}
        .cover{width:180px; height:auto; display:block; border-radius:6px}
        .title{font-size:16px; font-weight:600; margin:10px 0 6px; text-align:center}
        .meta{font-size:13px; color:#6b7280; margin:3px 0}
        .actions{margin-top:10px; display:flex; gap:10px; flex-direction:column; width:100%; align-items:center}
        .btn{background:#2563eb; color:#fff; border:none; padding:8px 16px; border-radius:6px; cursor:pointer; text-decoration:none; font-size:14px; transition:background 0.2s; width:120px; text-align:center}
        .btn:hover{background:#1d4ed8}
        .btn-success{background:#059669}
        .btn-success:hover{background:#047857}
        .empty{text-align:center; padding:40px 0; color:#6b7280}
        .link{color:#2563eb; text-decoration:none}
    </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">

    <div class="page-header">
        <h1>分类: <%= request.getAttribute("categoryName") %></h1>
        <p>该分类下的所有图书</p>
    </div>

    <%
        List<BookCategoryDetail> books = (List<BookCategoryDetail>) request.getAttribute("books");
        if (books == null) books = Collections.emptyList();
    %>

    <%
        if (books.isEmpty()) {
    %>
        <div class="empty">该分类下暂无图书</div>
    <%
        } else {
    %>
        <div class="grid">
            <%
                for (BookCategoryDetail book : books) {
                    String isbn = book.getIsbn() != null ? book.getIsbn().trim() : "";
                    String title = book.getTitle() != null ? book.getTitle() : "未知";
                    String author = book.getAuthor() != null ? book.getAuthor() : "未知";
                    String isbnFile = isbn.replaceAll("[^0-9Xx]", "");
                    String cover = request.getContextPath() + "/covers/" + isbnFile + ".jpg";
                    String encIsbn = java.net.URLEncoder.encode(isbn, java.nio.charset.StandardCharsets.UTF_8);
            %>
            <div class="card">
                <img class="cover" src="<%=cover%>" alt="封面" onerror="this.onerror=null;this.src='data:image/svg+xml;charset=UTF-8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"180\" height=\"260\"><rect width=\"100%\" height=\"100%\" fill=\"%23e5e7eb\"/><text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\" fill=\"%239ca3af\" font-family=\"Arial\" font-size=\"14\">无封面</text></svg>'"/>
                <div class="title"><%=title%></div>
                <div class="meta">作者：<%=author%></div>
                <div class="meta">ISBN：<%=isbn%></div>
                <div class="meta">分类：<%=book.getCategoryPath() != null ? book.getCategoryPath() : "暂无分类"%></div>
                <div class="actions">
                    <a class="btn" href="<%=request.getContextPath()%>/comment/list?isbn=<%=encIsbn%>">查看评论</a>
                    <a class="btn btn-success" href="<%=request.getContextPath()%>/book/physical?isbn=<%=encIsbn%>">查看实体书</a>
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

