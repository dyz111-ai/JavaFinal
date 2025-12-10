<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.book.model.BookInfo" %>
<%@ page import="com.example.demo0.home.model.Announcement" %>
<%@ page import="java.text.SimpleDateFormat" %>
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

        /* 为导航栏留出空间 */
        body { padding-top: 5rem; }
        
        /* Banner 区（轮播 + 搜索条） */
        .banner{ position:relative; width:100%; height:68vh; min-height:420px; background:#eef5fb; overflow:hidden; margin-top: 0; }
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
        .grid-3{ grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); } /* 自动适应宽度 */
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
        .announce-title{ font-weight:600; display:flex; justify-content:space-between; align-items:center; }
        .announce-date{ font-size:13px; color:var(--sub); font-weight:normal; }
        .announce-content{ color:var(--sub); margin-top:4px; font-size:14px }

        /* 推荐图书 */
        .rec-card{ display:flex; flex-direction:column; align-items:center }
        .cover{ width:180px; height:auto; display:block; border-radius:6px }
        .title{ font-size:16px; font-weight:600; margin:10px 0 6px; text-align:center }
        .meta{ font-size:13px; color:var(--sub); margin:3px 0 }
        .actions{ margin-top:8px }
        .main-layout { display: flex; gap: 20px; align-items: flex-start; margin-top: 24px; }
        .main-content { flex: 1; min-width: 0; }
    </style>
</head>
<body>
<%
    // 服务端数据
    List<BookInfo> recommends = (List<BookInfo>) request.getAttribute("recommends");
    if (recommends == null) recommends = Collections.emptyList();
    List<Announcement> announcements = (List<Announcement>) request.getAttribute("announcements");
    if (announcements == null) announcements = Collections.emptyList();
    List<Map<String, String>> quickEntries = (List<Map<String, String>>) request.getAttribute("quickEntries");
    if (quickEntries == null) quickEntries = Collections.emptyList();
%>

<%-- 顶部导航栏 --%>

    

        <div class="nav-links" style="display: flex; align-items: center; gap: 24px;">
            <%-- 基础导航 --%>
            <%@ include file="/WEB-INF/common/navbar.jsp" %>
        </div>
   


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

<div class="container main-layout">
       
        <main class="main-content">
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
                    <a class="entry-link" href="<%=request.getContextPath()%>/reader/borrow-records">进入</a>
                </div>
                <div class="entry-card">
                    <div class="entry-left">
                        <img src="<%=request.getContextPath()%>/assets/icons/book.svg" alt="分类">
                        <span>图书分类</span>
                    </div>
                    <a class="entry-link" href="<%=request.getContextPath()%>/category/display">进入</a>
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
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    for (Announcement a : announcements) {
                        String t = a.getTitle() != null ? a.getTitle() : "";
                        String c = a.getContent() != null ? a.getContent() : "";
                        String d = a.getCreateTime() != null ? sdf.format(a.getCreateTime()) : "";
            %>
            <div class="announce-item">
                <div class="announce-title">
                    <span><%= t %></span>
                    <span class="announce-date"><%= d %></span>
                </div>
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
                <img class="cover" src="<%=cover%>" alt="封面" onerror="this.onerror=null;this.src='data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns=%22http://www.w3.org/2000/svg%22%20width=%22180%22%20height=%22260%22%3E%3Crect%20width=%22100%25%22%20height=%22100%25%22%20fill=%22%23e5e7eb%22/%3E%3Ctext%20x=%2250%25%22%20y=%2250%25%22%20dominant-baseline=%22middle%22%20text-anchor=%22middle%22%20fill=%22%239ca3af%22%20font-family=%22Arial%22%20font-size=%2214%22%3E%E6%97%A0%E5%B0%81%E9%9D%A2%3C/text%3E%3C/svg%3E';"/>
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
</main>
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