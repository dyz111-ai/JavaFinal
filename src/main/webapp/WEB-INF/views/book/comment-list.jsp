<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.CommentRecord" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <title>评论列表</title>
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Microsoft YaHei",sans-serif;margin:0;padding:20px;padding-top:5rem;background:#f7f7f7;color:#374151}
    .container{max-width:900px;margin:0 auto}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:18px;margin-bottom:14px}
    .muted{color:#6b7280}
    .title{font-size:18px;font-weight:700;margin:0 0 10px}
    .row{display:flex;justify-content:space-between;align-items:center}
    .rating{color:#f59e0b}
    a{color:#2563eb;text-decoration:none}
    textarea,input,select{border:1px solid #e5e7eb;border-radius:8px;padding:10px 12px;width:100%;}
    .btn{background:#2563eb;color:#fff;border:none;padding:10px 14px;border-radius:8px;cursor:pointer}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
  <h2>评论列表</h2>
  <%
    String isbn = (String) request.getAttribute("isbn");
    if (isbn == null) isbn = request.getParameter("isbn");
    List<CommentRecord> comments = (List<CommentRecord>) request.getAttribute("comments");
    if (comments == null) comments = Collections.emptyList();
    // ctx变量已在navbar.jsp中定义，这里直接使用
    String redirect = "/comment/list?isbn=" + (isbn==null?"":isbn);
  %>
  <div class="card">
    <div class="row">
      <div>ISBN：<strong><%= isbn == null? "" : isbn %></strong></div>
      <div>
        <a href="<%=ctx%>/book/search">返回搜索</a>
      </div>
    </div>
  </div>

  <!-- 新增评论表单（读者ID在后端写死为1） -->
  <div class="card">
    <div class="title">发表你的评论</div>
    <form id="add" method="post" action="<%=ctx%>/comment/add">
      <input type="hidden" name="ISBN" value="<%= isbn==null?"":isbn %>
      <input type="hidden" name="redirect" value="<%= redirect %>">
      <label style="font-size:13px;color:#6b7280">评分（1-5）</label>
      <select name="Rating" required>
        <option value="5">5 - 强烈推荐</option>
        <option value="4">4 - 推荐</option>
        <option value="3">3 - 一般</option>
        <option value="2">2 - 较差</option>
        <option value="1">1 - 不推荐</option>
      </select>
      <div style="height:10px"></div>
      <label style="font-size:13px;color:#6b7280">评论内容</label>
      <textarea name="ReviewContent" rows="4" placeholder="写点什么吧..." required></textarea>
      <div style="height:12px"></div>
      <button class="btn" type="submit">提交评论</button>
    </form>
  </div>

  <%
    if (comments.isEmpty()) {
  %>
    <div class="card muted">暂无评论。</div>
  <%
    } else {
      for (CommentRecord c : comments) {
        String reader = c.getReaderId()==null? "匿名用户" : ("读者#"+c.getReaderId());
        String when = c.getCreateTime()==null? "" : c.getCreateTime().toString();
        String stars = "";
        if (c.getRating()!=null) {
          int r = Math.max(0, Math.min(5, c.getRating()));
          StringBuilder sb = new StringBuilder();
          for (int i=0;i<r;i++) sb.append("★");
          for (int i=r;i<5;i++) sb.append("☆");
          stars = sb.toString();
        }
  %>
    <div class="card">
      <div class="row">
        <div class="muted"><%= reader %></div>
        <div class="muted"><%= when %></div>
      </div>
      <div class="rating"><%= stars %></div>
      <p style="margin-top:8px; white-space:pre-wrap;"><%= c.getReviewContent()==null? "" : c.getReviewContent() %></p>
      <div class="muted">状态：<%= c.getStatus()==null? "" : c.getStatus() %></div>
    </div>
  <%
      }
    }
  %>
</div>
</body>
</html>