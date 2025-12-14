<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<style>
    .admin-navbar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    }
    
    .nav-container {
        max-width: 80rem;
        margin: 0 auto;
        padding: 0 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        height: 64px;
    }
    
    .nav-left {
        display: flex;
        align-items: center;
        gap: 32px;
    }
    
    .nav-brand {
        display: flex;
        align-items: center;
        gap: 8px;
        color: white;
        font-size: 20px;
        font-weight: 600;
        text-decoration: none;
    }
    
    .nav-brand-icon {
        width: 24px;
        height: 24px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 14px;
    }
    
    .nav-menu {
        display: flex;
        align-items: center;
        gap: 8px;
        list-style: none;
        margin: 0;
        padding: 0;
    }
    
    .nav-menu-item {
        position: relative;
    }
    
    .nav-menu-link {
        display: block;
        padding: 8px 16px;
        color: rgba(255, 255, 255, 0.9);
        text-decoration: none;
        font-size: 14px;
        font-weight: 500;
        border-radius: 6px;
        transition: all 0.2s ease;
        white-space: nowrap;
    }
    
    .nav-menu-link:hover {
        background-color: rgba(255, 255, 255, 0.15);
        color: white;
    }
    
    .nav-menu-link.active {
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
    }
    
    .nav-right {
        display: flex;
        align-items: center;
        gap: 16px;
    }
    
    .admin-label {
        color: rgba(255, 255, 255, 0.9);
        font-size: 14px;
        font-weight: 500;
        padding: 0;
        margin: 0;
    }
    
    .logout-btn {
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
        padding: 8px 20px;
        border-radius: 8px;
        text-decoration: none;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.2s ease;
        border: 1px solid rgba(255, 255, 255, 0.3);
        white-space: nowrap;
    }
    
    .logout-btn:hover {
        background-color: rgba(255, 255, 255, 0.3);
        border-color: rgba(255, 255, 255, 0.5);
        transform: translateY(-1px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    }
    
    @media (max-width: 768px) {
        .nav-menu {
            display: none;
        }
        
        .nav-container {
            padding: 0 16px;
        }
    }
</style>

<nav class="admin-navbar">
    <div class="nav-container">
        <div class="nav-left">
            <a href="<%= request.getContextPath() %>/admin/dashboard" class="nav-brand">
                <span>图书馆管理系统</span>
            </a>
            <ul class="nav-menu">
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/dashboard" class="nav-menu-link">仪表盘</a>
                </li>
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/books" class="nav-menu-link">图书管理</a>
                </li>
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/category" class="nav-menu-link">分类管理</a>
                </li>
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/book-category" class="nav-menu-link">图书分类</a>
                </li>
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/announcements" class="nav-menu-link">公告管理</a>
                </li>
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/report-handling" class="nav-menu-link">举报处理</a>
                </li>
                <li class="nav-menu-item">
                    <a href="<%= request.getContextPath() %>/admin/purchase-analysis" class="nav-menu-link">采购分析</a>
                </li>
            </ul>
        </div>
        <div class="nav-right">
            <span class="admin-label">管理员</span>
            <a href="<%= request.getContextPath() %>/auth/logout" class="logout-btn">
                退出登录
            </a>
        </div>
    </div>
</nav>

<script>
    // 高亮当前页面的导航链接
    (function() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-menu-link');
        
        navLinks.forEach(function(link) {
            const linkPath = link.getAttribute('href');
            if (currentPath === linkPath || currentPath.startsWith(linkPath + '/')) {
                link.classList.add('active');
            }
        });
    })();
</script>