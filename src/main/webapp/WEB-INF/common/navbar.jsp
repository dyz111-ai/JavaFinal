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
        </nav>
        
        <!-- 登录按钮（暂时不实现登录功能） -->
        <div class="auth-btn">
            <a href="#" class="login">登录</a>
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

/* ---------- 登录按钮 ---------- */
.auth-btn .login {
    color: #004b8d;
    background: #fff;
    padding: 0.4rem 1rem;
    border-radius: 6px;
    text-decoration: none;
    font-weight: 600;
    font-size: 0.9rem;
}

.auth-btn .login:hover { background: #f0f0f0; }
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

