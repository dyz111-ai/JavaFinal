<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.PhysicalBook" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <title>实体书信息</title>
    <style>
        body {
            font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Microsoft YaHei", sans-serif;
            margin: 0;
            padding: 20px;
            padding-top: 5rem;
            background: #f8fafc;
            color: #374151;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        .header-section {
            background: #ffffff;
            color: #333;
            padding: 24px;
            border-radius: 8px;
            margin-bottom: 25px;
            text-align: center;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            border: 1px solid #e5e7eb;
        }
        .header-section h1 {
            font-size: 24px;
            font-weight: 700;
            margin: 0 0 8px;
            color: #1f2937;
        }
        .header-section p {
            color: #6b7280;
            margin: 0;
            font-size: 14px;
        }
        .books-table {
            width: 100%;
            background: #ffffff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            border: 1px solid #e5e7eb;
        }
        .books-table table {
            width: 100%;
            border-collapse: collapse;
        }
        .books-table thead {
            background: #f9fafb;
        }
        .books-table th {
            padding: 12px 16px;
            text-align: left;
            font-weight: 600;
            font-size: 14px;
            color: #374151;
            border-bottom: 2px solid #e5e7eb;
        }
        .books-table td {
            padding: 12px 16px;
            border-bottom: 1px solid #e5e7eb;
            font-size: 14px;
            color: #4b5563;
        }
        .books-table tbody tr:hover {
            background: #f9fafb;
        }
        .books-table tr:last-child td {
            border-bottom: none;
        }
        .status-badge {
            display: inline-block;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: 500;
        }
        .status-normal {
            background: #d1fae5;
            color: #065f46;
        }
        .status-borrowed {
            background: #fee2e2;
            color: #991b1b;
        }
        .status-offline {
            background: #f3f4f6;
            color: #4b5563;
        }
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #6b7280;
        }
        .empty-state p {
            font-size: 16px;
            margin: 0;
        }
        .link {
            color: #2563eb;
            text-decoration: none;
        }
        .link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
    <%
        String bookTitle = (String) request.getAttribute("bookTitle");
        List<PhysicalBook> books = (List<PhysicalBook>) request.getAttribute("books");
        if (books == null) books = Collections.emptyList();
    %>

    <div class="header-section">
        <h1>《<%= bookTitle != null ? bookTitle : "未知" %>》实体书信息</h1>
        <p>共找到 <%= books.size() %> 本实体书</p>
    </div>

    <%
        if (books.isEmpty()) {
    %>
        <div class="empty-state">
            <p>未找到相关实体书</p>
        </div>
    <%
        } else {
    %>
        <div class="books-table">
            <table>
                <thead>
                    <tr>
                        <th>条码</th>
                        <th>楼宇</th>
                        <th>书架编号</th>
                        <th>楼层</th>
                        <th>区域</th>
                        <th>状态</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (PhysicalBook book : books) {
                            String status = book.getStatus() != null ? book.getStatus() : "未知";
                            String statusClass = "status-normal";
                            if ("借出".equals(status) || "borrowed".equalsIgnoreCase(status)) {
                                statusClass = "status-borrowed";
                            } else if ("下架".equals(status) || "offline".equalsIgnoreCase(status)) {
                                statusClass = "status-offline";
                            }
                    %>
                    <tr>
                        <td><%= book.getBarcode() != null ? book.getBarcode() : "-" %></td>
                        <td><%= book.getBuildingNameDisplay() %></td>
                        <td><%= book.getShelfCode() != null ? book.getShelfCode() : "-" %></td>
                        <td><%= book.getFloor() != null ? book.getFloor() : "-" %></td>
                        <td><%= book.getZone() != null ? book.getZone() : "-" %></td>
                        <td>
                            <span class="status-badge <%= statusClass %>"><%= status %></span>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
    <%
        }
    %>
</div>
</body>
</html>




