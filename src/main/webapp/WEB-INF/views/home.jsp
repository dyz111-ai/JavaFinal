<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.BookInfo" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <title>图书馆首页</title>
    <!-- 复用原前端的基础样式，尽量保持观感一致 -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/base.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/main.css" />
    <style>
        :root{ --bg:#f7f7f7; --text:#374151; --sub:#6b7280; --brand:#2563eb; --card:#ffffff; --border:#e5e7eb }
        *{ box-sizing:border-box }
        body{ margin:0; color:var(--text); }
        a{ color:inherit; text-decoration:none }
        .container{ max-width:1100px; margin:0 auto; padding:20px }
        header{ display:flex; align-items:center; justify-content:space-between; padding:14px 0 }
        .brand{ font-weight:700; font-size:20px }
        .link{ color:var(--brand) }

        /* Banner 区（轮播 + 搜索条） */
        .banner{ position:relative; width:100%; height:68vh; min-height:420px; background:#eef5fb; overflow:hidden }
        .banner-slide{ position:absolute; inset:0; background-size:cover; background-position:center; opacity:0; transition:opacity .8s ease }
        .banner-slide.active{ opacity:1 }
        .banner-center{ position:absolute; left:50%; top:58%; transform:translate(-50%,-50%); width:80%; max-width:700px; z-index:2 }
        .search-bar{ display:flex; gap:8px; }
        .search-bar input[type=text]{ flex:1; padding:12px 14px; border:1px solid var(--border); border-radius:10px; background:#fff }
        .btn{ background:var(--brand); color:#fff; border:none; padding:12px 16px; border-radius:10px; cursor:pointer }
        .banner-dots{ position:absolute; left:50%; bottom:16px; transform:translateX(-50%); display:flex; gap:8px; z-index:2 }
        .dot{ width:10px; height:10px; border-radius:50%; background:rgba(255,255,255,.6); border:1px solid rgba(0,0,0,.1) }
        .dot.active{ background:#fff }

        /* 快速入口 */
        .section{ margin:24px 0 }
        .section h3{ margin:0 0 12px; font-size:18px }
        .grid{ display:grid; gap:18px }
        .grid-2{ grid-template-columns: repeat(2, minmax(0, 1fr)); }
        .grid-3{ grid-template-columns: repeat(3, minmax(0, 1fr)); }
        .grid-4{ grid-template-columns: repeat(4, minmax(0, 1fr)); }
        @media (max-width: 900px){ .grid-4{ grid-template-columns: repeat(2, minmax(0, 1fr)); } .grid-3{ grid-template-columns: repeat(2, minmax(0, 1fr)); } }
        @media (max-width: 600px){ .grid-4,.grid-3,.grid-2{ grid-template-columns: repeat(1, minmax(0, 1fr)); } }
        .card{ background:#fff; border:1px solid var(--border); border-radius:12px; padding:16px }
        .entry-card{ display:flex; align-items:center; justify-content:space-between; padding:16px; }
        .entry-left{ display:flex; align-items:center; gap:10px }
        .entry-left img{ width:24px; height:24px }
        .entry-link{ color:var(--brand) }

        /* 公告 */
        .announce-item{ padding:10px 0; border-top:1px dashed var(--border) }
        .announce-item:first-child{ border-top:none }
        .announce-title{ font-weight:600 }
        .announce-content{ color:var(--sub); margin-top:4px; font-size:14px }

        /* 推荐图书 */
        .rec-card{ display:flex; flex-direction:column; align-items:center }
        .cover{ width:180px; height:auto; display:block; border-radius:6px }
        .title{ font-size:16px; font-weight:600; margin:10px 0 6px; text-align:center }
        .meta{ font-size:13px; color:var(--sub); margin:3px 0 }
        .actions{ margin-top:8px }
    </style>
</head>
<body>
<%
    // 服务端数据
    List<BookInfo> recommends = (List<BookInfo>) request.getAttribute("recommends");
    if (recommends == null) recommends = Collections.emptyList();
    List<Map<String, String>> announcements = (List<Map<String, String>>) request.getAttribute("announcements");
    if (announcements == null) announcements = Collections.emptyList();
    List<Map<String, String>> quickEntries = (List<Map<String, String>>) request.getAttribute("quickEntries");
    if (quickEntries == null) quickEntries = Collections.emptyList();
%>

<%-- 顶部导航栏 --%>
<nav class="navbar">
    <div class="container navbar-inner">
        <a class="brand" href="<%=request.getContextPath()%>/home">
            <%-- 这里可以用 SVG 图标 --%>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #2563eb;">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
            </svg>
            <span style="font-weight: 700; font-size: 1.2rem; color: #1e293b;">阅享图书馆</span>
        </a>

        <div class="nav-links" style="display: flex; align-items: center; gap: 24px;">
            <%-- 基础导航 --%>
            <a class="nav-link" href="<%=request.getContextPath()%>/home">首页</a>
            <a class="nav-link" href="<%=request.getContextPath()%>/book/search">图书搜索</a>

            <%-- 分割线 --%>
            <div style="width: 1px; height: 24px; background-color: #e5e7eb;"></div>

            <%-- 登录状态判断 --%>
            <% if(session.getAttribute("currentUser") == null) { %>
            <%-- 未登录状态：显示主要的操作按钮 --%>
            <div style="display: flex; gap: 12px;">
                <a href="<%=request.getContextPath()%>/auth/login"
                   style="padding: 8px 20px; color: #4b5563; font-weight: 500; transition: color 0.2s;">
                    登录
                </a>
                <a href="<%=request.getContextPath()%>/auth/register" class="btn btn-primary"
                   style="padding: 8px 20px; border-radius: 99px; font-size: 0.95rem;">
                    注册账号
                </a>
            </div>
            <% } else { %>
            <%-- 已登录状态：显示用户信息和下拉菜单入口 --%>
            <div style="display: flex; align-items: center; gap: 16px;">
                <%-- 欢迎语 --%>
                <span style="font-size: 0.9rem; color: #64748b;">
                        你好, <strong style="color: #334155;">${sessionScope.currentUser.nickname}</strong>
                    </span>

                <%-- 个人中心按钮 (带图标) --%>
                <a href="<%=request.getContextPath()%>/reader/profile" class="nav-link" title="个人中心"
                   style="display: flex; align-items: center; gap: 6px; background: #f1f5f9; padding: 6px 12px; border-radius: 8px;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                    <span style="font-size: 0.9rem;">我的</span>
                </a>

                <%-- 退出按钮 --%>
                <a href="<%=request.getContextPath()%>/auth/logout"
                   style="color: #94a3b8; transition: color 0.2s;" title="退出登录">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                        <polyline points="16 17 21 12 16 7"></polyline>
                        <line x1="21" y1="12" x2="9" y2="12"></line>
                    </svg>
                </a>
            </div>
            <% } %>
        </div>
    </div>
</nav>

<!-- Banner 与搜索条 -->
<section class="banner">
    <div class="banner-slide active" style="background-image:url('<%=request.getContextPath()%>/assets/banner1.jpg')"></div>
    <div class="banner-slide" style="background-image:url('<%=request.getContextPath()%>/assets/banner2.jpg')"></div>
    <div class="banner-slide" style="background-image:url('<%=request.getContextPath()%>/assets/banner3.jpg')"></div>
    <div class="banner-slide" style="background-image:url('<%=request.getContextPath()%>/assets/banner4.jpg')"></div>
    <div class="banner-slide" style="background-image:url('<%=request.getContextPath()%>/assets/banner5.jpg')"></div>

    <div class="banner-center">
        <form class="search-bar" method="get" action="<%=request.getContextPath()%>/book/search">
            <input type="text" name="keyword" placeholder="输入书名 / 作者 / ISBN / 分类 进行搜索" />
            <button class="btn" type="submit">搜索</button>
        </form>
    </div>

    <div class="banner-dots">
        <div class="dot active"></div>
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
    </div>
</section>

<div class="container">
    <!-- 快速入口 + 公告 -->
    <div class="grid grid-2 section">
        <!-- 快速入口卡片 -->
        <section class="card">
            <h3>快速入口</h3>
            <div class="grid grid-3" style="margin-top:10px;">
                <div class="entry-card">
                    <div class="entry-left">
                        <img src="<%=request.getContextPath()%>/assets/icons/search.svg" alt="搜索">
                        <span>图书搜索</span>
                    </div>
                    <a class="entry-link" href="<%=request.getContextPath()%>/book/search">进入</a>
                </div>
                <div class="entry-card">
                    <div class="entry-left">
                        <img src="<%=request.getContextPath()%>/assets/icons/book.svg" alt="推荐">
                        <span>推荐图书</span>
                    </div>
                    <a class="entry-link" href="#recommend">查看</a>
                </div>
                <div class="entry-card">
                    <div class="entry-left">
                        <img src="<%=request.getContextPath()%>/assets/icons/renew.svg" alt="借阅">
                        <span>我的借阅</span>
                    </div>
                    <a class="entry-link" href="#">进入</a>
                </div>
            </div>
        </section>

        <!-- 公告 -->
        <section class="card">
            <h3>公告</h3>
            <%
                if (announcements.isEmpty()) {
            %>
            <div class="announce-item">暂无公告</div>
            <%
                } else {
                    for (Map<String, String> a : announcements) {
                        String t = a.getOrDefault("title", "");
                        String c = a.getOrDefault("content", "");
            %>
            <div class="announce-item">
                <div class="announce-title"><%= t %></div>
                <div class="announce-content"><%= c %></div>
            </div>
            <%
                    }
                }
            %>
        </section>
    </div>

    <!-- 推荐图书 -->
    <section class="section" id="recommend">
        <h3>推荐图书</h3>
        <div class="grid grid-4" style="margin-top:10px;">
            <%
                for (BookInfo b : recommends) {
                    String cover = request.getContextPath()+"/covers/"+b.getISBN()+".jpg";
                    String q = java.net.URLEncoder.encode(b.getTitle(), java.nio.charset.StandardCharsets.UTF_8);
            %>
            <div class="card rec-card">
                <img class="cover" src="<%=cover%>" alt="封面" onerror="this.onerror=null;this.src='data:image/svg+xml;charset=UTF-8,<svg xmlns='http://www.w3.org/2000/svg' width='180' height='260'><rect width='100%' height='100%' fill='%23e5e7eb'/><text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-family='Arial' font-size='14'>无封面</text></svg>'"/>
                <div class="title"><%= b.getTitle() %></div>
                <div class="meta">作者：<%= b.getAuthor() %></div>
                <div class="meta">ISBN：<%= b.getISBN() %></div>
                <div class="actions">
                    <a class="link" href="<%=request.getContextPath()%>/book/search?keyword=<%=q%>">去搜索</a>
                </div>
            </div>
            <%
                }
            %>
        </div>
    </section>
</div>

<script>
  // 简易轮播：切换 banner 背景图，模拟原前端 Banner 组件
  (function(){
    const slides = document.querySelectorAll('.banner-slide');
    const dots = document.querySelectorAll('.dot');
    let idx = 0;
    function show(i){
      slides.forEach((el, n)=> el.classList.toggle('active', n===i));
      dots.forEach((el, n)=> el.classList.toggle('active', n===i));
    }
    function next(){ idx = (idx + 1) % slides.length; show(idx); }
    setInterval(next, 4000);
    show(0);
  })();
</script>
</body>
</html>