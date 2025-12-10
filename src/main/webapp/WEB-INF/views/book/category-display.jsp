<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.CategoryNode" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <title>图书分类</title>
    <style>
        body{font-family:system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Microsoft YaHei", sans-serif; margin:0; padding:20px; padding-top:5rem; background:#f7f7f7; color:#374151}
        .container{max-width:1100px; margin:0 auto}
        .page-header{margin-bottom:24px}
        .page-header h1{font-size:24px; font-weight:700; margin:0 0 8px}

        /* 面包屑导航样式 */
        .breadcrumb { display: flex; align-items: center; font-size: 14px; color: #6b7280; margin-bottom: 16px; background: #fff; padding: 12px 16px; border-radius: 8px; border: 1px solid #e5e7eb; }
        .breadcrumb a { color: #2563eb; text-decoration: none; }
        .breadcrumb a:hover { text-decoration: underline; }
        .breadcrumb .separator { margin: 0 8px; color: #9ca3af; }
        .breadcrumb .current { color: #111827; font-weight: 500; }

        .category-container{background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:20px; box-shadow:0 2px 4px rgba(0,0,0,0.1)}
        .category-list{padding:16px}
        .category-item{padding:16px; border:1px solid #e5e7eb; border-radius:8px; margin-bottom:12px; background:#fff; font-weight:500; color:#374151; cursor:pointer; transition:all 0.2s; display: flex; justify-content: space-between; align-items: center;}
        .category-item:hover{background-color:#f9fafb; border-color:#2563eb; transform: translateY(-1px); box-shadow: 0 2px 4px rgba(0,0,0,0.05);}
        .category-name{font-size: 16px;}
        .category-meta{color:#9ca3af; font-size:13px; display: flex; align-items: center;}
        .arrow-icon { width: 16px; height: 16px; margin-left: 8px; }
        .empty{text-align:center; padding:40px; color:#6b7280}
    </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
    <div class="page-header">
        <h1>图书分类浏览</h1>
    </div>

    <%
        List<CategoryNode> list = (List<CategoryNode>) request.getAttribute("categoryList");
        List<String> path = (List<String>) request.getAttribute("categoryPath");
        CategoryNode currentCategory = (CategoryNode) request.getAttribute("currentCategory");
        Boolean isRoot = (Boolean) request.getAttribute("isRoot");
        if (list == null) list = Collections.emptyList();
    %>

    <div class="breadcrumb">
        <a href="<%=request.getContextPath()%>/category/display">全部分类</a>
        <%
            if (path != null && !path.isEmpty()) {
                // 这里 path 列表包含当前节点，我们只显示父路径
                // 如果 path 是 [根, 父, 当前]，展示逻辑如下
                for (int i = 0; i < path.size(); i++) {
        %>
        <span class="separator">/</span>
        <% if (i == path.size() - 1) { %>
        <span class="current"><%=path.get(i)%></span>
        <% } else { %>
        <span style="color:#6b7280"><%=path.get(i)%></span>
        <% } %>
        <%
                }
            }
        %>
    </div>

    <div class="category-container">
        <% if (isRoot != null && !isRoot && currentCategory != null) { %>
        <div style="padding: 0 16px; margin-bottom: 10px;">
            <h3 style="margin:0; font-size:18px;">
                <%=currentCategory.getCategoryName()%>
                <span style="font-size:14px; font-weight:normal; color:#6b7280; margin-left:8px;">下级分类</span>
            </h3>
        </div>
        <% } %>

        <%
            if (list.isEmpty()) {
        %>
        <div class="empty">该分类下暂无子分类</div>
        <%
        } else {
        %>
        <div class="category-list">
            <%
                for (CategoryNode category : list) {
                    String categoryId = category.getCategoryId();
                    String categoryName = category.getCategoryName();
                    // 判断是否有子节点，用于显示不同图标（可选）
                    boolean hasChildren = category.getChildren() != null && !category.getChildren().isEmpty();
            %>
            <div class="category-item" onclick="goToCategory('<%=categoryId%>')">
                <span class="category-name"><%=categoryName%></span>
                <div class="category-meta">
                    <% if(hasChildren) { %>
                    <span>下级分类</span>
                    <% } else { %>
                    <span>查看图书</span>
                    <% } %>
                    <svg class="arrow-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path></svg>
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
</div>

<script>
    function goToCategory(id) {
        // 无论是父分类还是子分类，统一跳转到 display 控制器
        // 控制器会根据是否有子节点决定是显示子列表还是跳转图书页
        const ctx = '<%=request.getContextPath()%>';
        window.location.href = ctx + '/category/display?categoryId=' + encodeURIComponent(id);
    }
</script>
</body>
</html>