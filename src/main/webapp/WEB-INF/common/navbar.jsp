<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
    String currentPath = request.getServletPath();
    if (currentPath == null) currentPath = "";
%>
<header class="navbar" id="navbar">
    <div class="navbar-container">
        <a href="<%=ctx%>/home" class="logo">Library System</a>
        
        <nav class="nav-links">
            <a href="<%=ctx%>/home" class="nav-item <%=currentPath.equals("/home") || currentPath.equals("") ? "active" : ""%>">首页</a>
            <a href="<%=ctx%>/book/search" class="nav-item <%=currentPath.startsWith("/book/search") ? "active" : ""%>">图书搜索</a>
            <a href="<%=ctx%>/category/display" class="nav-item <%=currentPath.startsWith("/category") ? "active" : ""%>">图书分类</a>
            <a href="<%=ctx%>/reader/borrow-records" class="nav-item <%=currentPath.startsWith("/reader/borrow-") ? "active" : ""%>">我的借阅</a>
            <a href="<%=ctx%>/reader/booklists" class="nav-item <%=currentPath.startsWith("/reader/booklists") ? "active" : ""%>">个性化推荐</a>
            <a href="<%=ctx%>/reader/space" class="nav-item <%=currentPath.startsWith("/reader/space") ? "active" : ""%>">座位预约</a>
        </nav>
        
        <div class="nav-actions">
            <div class="nav-divider"></div>
            <% if(session.getAttribute("currentUser") == null) { %>
                <a href="<%=ctx%>/auth/login" class="action-link">登录</a>
                <a href="<%=ctx%>/auth/register" class="action-primary">注册账号</a>
            <% } else { %>
                <span class="welcome-text">你好，<strong>${sessionScope.currentUser.nickname}</strong></span>
                <a href="<%=ctx%>/reader/profile" class="nav-pill" title="个人中心">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span>我的</span>
                </a>
                <a href="<%=ctx%>/auth/logout" class="icon-btn" title="退出登录">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                </a>
            <% } %>
        </div>
    </div>
</header>

<style>
/* ---------- NAVBAR 基础 ---------- */
.navbar {
    position: fixed;
    top: 0;
    width: 100%;
    z-index: 1000;
    background-color: transparent;
    transition: background 0.3s ease, box-shadow 0.3s ease, height 1s ease;
    box-shadow: none;
    height: 5rem;
}

/* 顶部渐变遮罩增强可读性 */
.navbar::before {
    content: "";
    position: absolute;
    inset: 0;
    background: linear-gradient(rgba(0,0,0,0.6), rgba(0,0,0,0));
    pointer-events: none;
    opacity: 1;
    transition: opacity .3s ease;
}

.navbar.scrolled {
    background-color: #004b8d;
    box-shadow: 0 2px 6px rgba(0,0,0,.1);
    height: 4.5rem;
}
.navbar.scrolled::before { opacity: 0; }

.navbar-container {
    max-width: 1200px;
    margin: auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 1rem;
    height: 100%;
}

.logo {
    font-size: 1.5rem;
    font-weight: 700;
    color: #fff;
    text-decoration: none;
}

/* ---------- 链接区 ---------- */
.nav-links {
    display: flex;
    gap: 1rem;
    align-items: center;
}

.nav-item {
    width: 120px;
    text-align: center;
    color: #fff;
    font-size: 0.85rem;
    font-weight: 700;
    text-decoration: none;
    padding: 0.3rem 0.5rem;
    border-radius: 6px;
    position: relative;
    cursor: pointer;
    line-height: 1;
    text-shadow: 0 0 4px rgba(0,0,0,0.6);
}

/* 下划线动画 */
.nav-item::after {
    content: "";
    position: absolute;
    left: 50%;
    transform: translateX(-50%) scaleX(0);
    bottom: 0.1rem;
    height: 2px;
    width: 50%;
    background: #ffffff;
    opacity: 0;
    transition: transform 0.25s ease, opacity 0.25s ease;
    transform-origin: center;
    pointer-events: auto;
}

.nav-item:hover::after,
.nav-item.active::after {
    transform: translateX(-50%) scaleX(1);
    opacity: 1;
}

.nav-item:hover { background: transparent; }

.nav-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
}

.nav-divider {
    width: 1px;
    height: 24px;
    background-color: rgba(255,255,255,0.35);
}

.action-link {
    color: #e2e8f0;
    font-weight: 600;
    text-decoration: none;
    padding: 0.4rem 0.9rem;
    border-radius: 999px;
    transition: color .2s ease, background .2s ease;
}

.action-link:hover { background: rgba(255,255,255,0.12); color: #fff; }

.action-primary {
    color: #004b8d;
    background: #fff;
    padding: 0.45rem 1.1rem;
    border-radius: 999px;
    text-decoration: none;
    font-weight: 700;
    font-size: 0.9rem;
    box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

.action-primary:hover { background: #f8fafc; }

.welcome-text {
    color: #e2e8f0;
    font-size: 0.9rem;
    display: flex;
    gap: 0.2rem;
}

.nav-pill {
    display: inline-flex;
    align-items: center;
    gap: 0.35rem;
    padding: 0.45rem 0.9rem;
    border-radius: 10px;
    background: rgba(255,255,255,0.12);
    color: #fff;
    text-decoration: none;
    font-weight: 600;
}

.nav-pill:hover { background: rgba(255,255,255,0.2); }

.icon-btn {
    color: #e2e8f0;
    display: inline-flex;
    align-items: center;
    padding: 0.35rem;
    border-radius: 8px;
    transition: background .2s ease, color .2s ease;
}

.icon-btn:hover { background: rgba(255,255,255,0.12); color: #fff; }
</style>

<script>
// 导航栏滚动效果
(function() {
    const navbar = document.getElementById('navbar');
    if (!navbar) return;
    
    function handleScroll() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        if (scrollTop > 0) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    }
    
    window.addEventListener('scroll', handleScroll, { passive: true });
    handleScroll(); // 初始化
})();
</script>

