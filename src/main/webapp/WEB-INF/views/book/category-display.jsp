<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.CategoryNode" %>
<%!
    // 递归查找所有叶子节点（最终分类）
    private void findLeafCategories(List<CategoryNode> nodes, List<CategoryNode> leaves) {
        if (nodes == null) return;
        for (CategoryNode node : nodes) {
            if (node.getChildren() == null || node.getChildren().isEmpty()) {
                // 叶子节点，添加到列表
                leaves.add(node);
            } else {
                // 有子节点，继续递归查找
                findLeafCategories(node.getChildren(), leaves);
            }
        }
    }
%>
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
        .page-header p{color:#6b7280; margin:0}
        .category-container{background:#fff; border:1px solid #e5e7eb; border-radius:12px; padding:20px; box-shadow:0 2px 4px rgba(0,0,0,0.1)}
        .category-list{padding:16px}
        .category-item{padding:12px 16px; border:1px solid #e5e7eb; border-radius:6px; margin-bottom:8px; background:#fff; font-weight:500; color:#374151; cursor:pointer; transition:background-color 0.2s}
        .category-item:hover{background-color:#f3f4f6}
        .category-name{display:inline-block}
        .category-id{color:#6b7280; font-size:13px; margin-left:8px}
        .empty{text-align:center; padding:40px; color:#6b7280}
    </style>
</head>
<body>
<%@ include file="/WEB-INF/common/navbar.jsp" %>
<div class="container">
    <div class="page-header">
        <h1>图书分类</h1>
    </div>

    <div class="category-container">
        <%
            List<CategoryNode> tree = (List<CategoryNode>) request.getAttribute("categoryTree");
            if (tree == null) tree = Collections.emptyList();
            
            // 查找所有叶子节点（最终分类）
            List<CategoryNode> leafCategories = new ArrayList<>();
            findLeafCategories(tree, leafCategories);
        %>

        <%
            if (leafCategories.isEmpty()) {
        %>
            <div class="empty">暂无分类数据</div>
        <%
            } else {
        %>
            <div class="category-list">
                <%
                    for (CategoryNode category : leafCategories) {
                        String categoryId = category.getCategoryId() != null ? category.getCategoryId() : "";
                        String categoryName = category.getCategoryName() != null ? category.getCategoryName() : "未知分类";
                        // 转义JavaScript字符串中的特殊字符
                        String safeCategoryId = categoryId.replace("'", "\\'").replace("\"", "\\\"").replace("\\", "\\\\");
                        String safeCategoryName = categoryName.replace("'", "\\'").replace("\"", "\\\"").replace("\\", "\\\\");
                %>
                    <div class="category-item" 
                         data-category-id="<%=categoryId%>"
                         data-category-name="<%=categoryName%>"
                         onclick="goToCategoryBooks(this)">
                        <span class="category-name"><%=categoryName%></span>
                        <span class="category-id">(<%=categoryId%>)</span>
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
function goToCategoryBooks(element) {
    const categoryId = element.getAttribute('data-category-id');
    const categoryName = element.getAttribute('data-category-name');
    const ctx = '<%=request.getContextPath()%>';
    const url = ctx + '/category/books?categoryId=' + encodeURIComponent(categoryId) + 
                '&categoryName=' + encodeURIComponent(categoryName);
    window.location.href = url;
}
</script>
</body>
</html>
