<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.reader.model.BorrowRecordDetail" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8" />
  <title>我的借阅记录</title>
  <style>
    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Microsoft YaHei",sans-serif;margin:0;padding:20px;padding-top:5rem;background:#f7f7f7;color:#374151}
    .container{max-width:1200px;margin:0 auto}
    .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:24px;margin-bottom:20px}
    .title{font-size:28px;font-weight:bold;margin-bottom:20px;color:#2c3e50;text-align:center}
    .stats{display:flex;gap:20px;margin-bottom:20px;flex-wrap:wrap}
    .stat-item{flex:1;min-width:200px;background:#f9fbfd;padding:16px;border-radius:8px;border:1px solid #e5e7eb}
    .stat-label{font-size:14px;color:#6b7280;margin-bottom:8px}
    .stat-value{font-size:24px;font-weight:bold;color:#2563eb}
    .stat-value.overdue{color:#e57373}
    .actions{text-align:right;margin-bottom:15px}
    .btn{background:#2563eb;color:#fff;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-size:16px;text-decoration:none;display:inline-block}
    .btn:hover{background:#1d4ed8}
    .sort-container{margin-bottom:14px;display:flex;align-items:center;gap:8px}
    .sort-container select{padding:6px 12px;border:1px solid #e5e7eb;border-radius:6px;font-size:14px}
    .styled-table{width:100%;border-collapse:collapse;font-size:16px;border-radius:10px;overflow:hidden}
    .styled-table th{background-color:#4da6ff;color:white;text-align:center;padding:16px;font-size:17px}
    .styled-table td{padding:16px;border-bottom:1px solid #ddd;text-align:center;height:58px;font-size:16px}
    .styled-table tbody tr:nth-child(even){background-color:#f2f8ff}
    .styled-table tbody tr:hover{background-color:#e6f3ff}
    .overdue{color:#e57373;font-weight:bold}
    .no-data{text-align:center;color:#999;padding:40px;font-size:16px}
    .tabs{border-bottom:1px solid #e5e7eb;margin-bottom:20px;display:flex}
    .tab-item{padding:10px 20px;cursor:pointer;font-size:18px;font-weight:bold;color:#6b7280;text-decoration:none;border-bottom:3px solid transparent}
    .tab-item.active{color:#2563eb;border-bottom-color:#2563eb}
    .tab-item:hover{color:#2563eb}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
  <h1 class="title">📚 我的借阅</h1>

  <div class="tabs">
    <a href="<%=request.getContextPath()%>/reader/borrow-records" class="tab-item active">借阅记录</a>
    <a href="<%=request.getContextPath()%>/reader/borrow-return" class="tab-item">借还功能</a>
  </div>

  <%-- Page content starts here --%>
  <% 
    List<BorrowRecordDetail> records = (List<BorrowRecordDetail>) request.getAttribute("records");
    Integer unreturnedCount = (Integer) request.getAttribute("unreturnedCount");
    Integer overdueCount = (Integer) request.getAttribute("overdueCount");
    if (records == null) records = Collections.emptyList();
    if (unreturnedCount == null) unreturnedCount = 0;
    if (overdueCount == null) overdueCount = 0;
    
    String sortOrder = request.getParameter("sort");
    if (sortOrder == null) sortOrder = "desc";
    
    records = new ArrayList<>(records);
    if ("asc".equals(sortOrder)) {
      records.sort(Comparator.comparing(BorrowRecordDetail::getBorrowTime, Comparator.nullsLast(Comparator.naturalOrder())));
    } else {
      records.sort(Comparator.comparing(BorrowRecordDetail::getBorrowTime, Comparator.nullsLast(Comparator.reverseOrder())));
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  %>

  <!-- 统计信息 -->
  <div class="stats">
    <div class="stat-item">
      <div class="stat-label">未归还图书</div>
      <div class="stat-value"><%= unreturnedCount %></div>
    </div>
    <div class="stat-item">
      <div class="stat-label">逾期图书</div>
      <div class="stat-value overdue"><%= overdueCount %></div>
    </div>
    <div class="stat-item">
      <div class="stat-label">总借阅记录</div>
      <div class="stat-value"><%= records.size() %></div>
    </div>
  </div>

  <div class="card">
    <div class="actions">
      <button class="btn" onclick="location.reload()">🔄 刷新记录</button>
    </div>

    <% if (!records.isEmpty()) { %>
    <div class="sort-container">
      <label>排序方式：</label>
      <select onchange="location.href='?sort='+this.value">
        <option value="desc" <%= "desc".equals(sortOrder) ? "selected" : "" %>>借出时间降序</option>
        <option value="asc" <%= "asc".equals(sortOrder) ? "selected" : "" %>>借出时间升序</option>
      </select>
    </div>
    <% } %>

    <% if (records.isEmpty()) { %>
      <p class="no-data">暂无借阅记录</p>
    <% } else { %>
    <table class="styled-table">
      <thead>
        <tr>
          <th>ISBN</th>
          <th>书名</th>
          <th>作者</th>
          <th>借出时间</th>
          <th>归还时间</th>
          <th>逾期罚金</th>
        </tr>
      </thead>
      <tbody>
        <% for (BorrowRecordDetail record : records) { %>
        <tr>
          <td><%= record.getIsbn() != null ? record.getIsbn() : "" %></td>
          <td><%= record.getBookTitle() != null ? record.getBookTitle() : "" %></td>
          <td><%= record.getBookAuthor() != null ? record.getBookAuthor() : "" %></td>
          <td><%= record.getBorrowTime() != null ? record.getBorrowTime().format(formatter) : "" %></td>
          <% 
            String returnTimeStr = "待归还";
            boolean isOverdue = false;
            if (record.getReturnTime() != null) {
              returnTimeStr = record.getReturnTime().format(formatter);
            } else if (record.getBorrowTime() != null && record.getBorrowTime().isBefore(java.time.OffsetDateTime.now().minusDays(1))) {
              isOverdue = true;
              returnTimeStr = "已逾期";
            }
          %>
          <td class="<%= isOverdue ? "overdue" : "" %>"><%= returnTimeStr %></td>
          <td class="<%= record.getOverdueFine() != null && record.getOverdueFine().compareTo(java.math.BigDecimal.ZERO) > 0 ? "overdue" : "" %>">
            <%= String.format("%.2f", record.getOverdueFine() != null ? record.getOverdueFine() : java.math.BigDecimal.ZERO) %>
          </td>
        </tr>
        <% } %>
      </tbody>
    </table>
    <% } %>
  </div>
</div>
</body>
</html>