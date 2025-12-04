<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%
    // 将首页直接重定向到主界面（Home）
    response.sendRedirect(request.getContextPath() + "/home");
    return; // 确保不再渲染下面的内容
%>