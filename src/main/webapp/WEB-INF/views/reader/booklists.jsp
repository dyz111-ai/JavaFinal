<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.example.demo0.auth.model.Reader" %>
<%@ page import="java.util.*" %>
<%@ page import="com.example.demo0.reader.model.BookList" %>
<%@ page import="com.example.demo0.book.model.BookInfo" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <title>我的书单</title>
    <style>
        body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,"Microsoft YaHei",sans-serif;margin:0;padding:20px;padding-top:5rem;background:#f7f7f7;color:#374151}
        .container{max-width:1200px;margin:0 auto}
        .title{font-size:28px;font-weight:bold;margin-bottom:20px;color:#2c3e50}
        .reader-layout{display:flex;gap:20px;align-items:flex-start}
        .reader-content{flex:1;min-width:0}
        .card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:24px;margin-bottom:20px}
        .btn{background:#2563eb;color:#fff;border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-size:16px}
        .btn:hover{background:#1d4ed8}
        .btn-danger{background:#dc2626;color:#fff}
        .btn-danger:hover{background:#b91c1c}
        .form-group{margin-bottom:15px}
        .form-group label{display:block;margin-bottom:5px;font-weight:bold}
        .form-group input,.form-group textarea{width:100%;padding:10px;border:1px solid #ccc;border-radius:6px;box-sizing:border-box}
        .booklist-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:20px}
        .booklist-section{margin-bottom:30px}
        .booklist-section-title{font-size:20px;font-weight:bold;margin-bottom:15px;color:#2c3e50;padding-bottom:10px;border-bottom:2px solid #e5e7eb}
        .booklist-card{background:#fff;border:1px solid #e5e7eb;border-radius:12px;padding:20px;display:flex;flex-direction:column;justify-content:space-between}
        .booklist-name{font-size:18px;font-weight:bold;margin:0 0 10px}
        .booklist-intro{color:#6b7280;font-size:14px;flex-grow:1;margin-bottom:15px}
        .booklist-actions{display:flex;gap:10px}
        /* Modal styles */
        .modal{display:none;position:fixed;z-index:1000;left:0;top:0;width:100%;height:100%;overflow:auto;background-color:rgba(0,0,0,0.4)}
        .modal-content{background-color:#fefefe;margin:10% auto;padding:20px;border:1px solid #888;width:80%;max-width:700px;border-radius:12px}
        .close{color:#aaa;float:right;font-size:28px;font-weight:bold;cursor:pointer}
    </style>
</head>
<body>
<%
    Reader currentUser = (Reader) session.getAttribute("currentUser");
    Integer readerId = currentUser != null ? currentUser.getReaderId() : null;
    boolean loggedIn = readerId != null;
%>
<%@ include file="/WEB-INF/common/navbar.jsp" %>

<div class="container">
    <h1 class="title">我的书单</h1>

            <!-- 创建新书单 -->
            <div class="card">
                <h2>创建新书单</h2>
                <form id="createBooklistForm">
                    <div class="form-group">
                        <label for="newName">书单名称</label>
                        <input type="text" id="newName" required>
                    </div>
                    <div class="form-group">
                        <label for="newIntro">简介</label>
                        <textarea id="newIntro" rows="3"></textarea>
                    </div>
                    <button type="submit" class="btn">创建</button>
                </form>
            </div>

            <!-- 我的书单列表 -->
            <div class="card">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px">
                    <h2 style="margin:0">书单列表</h2>
                    <button class="btn" style="background:#10b981" onclick="showRecommendModal()">猜你喜欢</button>
                </div>
                <div id="booklistGrid" class="booklist-grid"></div>
            </div>
</div>

<!-- 详情/编辑 Modal -->
<div id="detailModal" class="modal">
    <div class="modal-content">
        <span class="close" onclick="closeModal()">&times;</span>
        <h2 id="modalTitle">书单详情</h2>
        <div id="modalBody"></div>
    </div>
</div>

<!-- 推荐书单 Modal -->
<div id="recommendModal" class="modal">
    <div class="modal-content">
        <span class="close" onclick="closeRecommendModal()">&times;</span>
        <h2>猜你喜欢</h2>
        <div id="recommendBody"></div>
    </div>
</div>

<script>
    const ctx = '<%=request.getContextPath()%>';
    const API_BASE = ctx + '/api/book/booklists';
    const API_MY = API_BASE + '/reader';
    const isLoggedIn = <%= loggedIn ? "true" : "false" %>;

    document.addEventListener('DOMContentLoaded', loadBooklists);

    async function loadBooklists() {
        if (!isLoggedIn) {
            document.getElementById('booklistGrid').innerHTML = '<p style="color:red;">请先登录后查看书单</p>';
            return;
        }
        try {
            const response = await fetch(API_MY);
            console.log('API Response status:', response.status);
            console.log('API Response URL:', response.url);
            if (!response.ok) {
                const errorText = await response.text();
                console.error('API Error:', errorText);
                document.getElementById('booklistGrid').innerHTML = '<p style="color:red;">加载失败: ' + errorText + '</p>';
                return;
            }
            const data = await response.json();
            console.log('Booklists data:', data);
            const grid = document.getElementById('booklistGrid');
            grid.innerHTML = '';
            
            // 数据结构：{ "Created": [...], "Collected": [...] }
            const created = data.Created || [];
            const collected = data.Collected || [];
            
            // 保存创建的书单ID集合，用于后续判断
            createdBooklistIds.clear();
            created.forEach(bl => {
                if (bl.BooklistId) createdBooklistIds.add(bl.BooklistId);
            });
            
            if (created.length === 0 && collected.length === 0) {
                grid.innerHTML = '<p>暂无书单，快去创建一个吧！</p>';
                return;
            }
            
            // 显示创建的书单
            if (created.length > 0) {
                const createdSection = document.createElement('div');
                createdSection.className = 'booklist-section';
                createdSection.innerHTML = '<h2 class="booklist-section-title">我创建的书单</h2>';
                const createdGrid = document.createElement('div');
                createdGrid.className = 'booklist-grid';
                created.forEach(bl => {
                    const card = createBooklistCard(bl, true);
                    if (card) createdGrid.appendChild(card);
                });
                createdSection.appendChild(createdGrid);
                grid.appendChild(createdSection);
            }
            
            // 显示收藏的书单
            if (collected.length > 0) {
                const collectedSection = document.createElement('div');
                collectedSection.className = 'booklist-section';
                collectedSection.innerHTML = '<h2 class="booklist-section-title">我收藏的书单</h2>';
                const collectedGrid = document.createElement('div');
                collectedGrid.className = 'booklist-grid';
                collected.forEach(bl => {
                    const card = createBooklistCard(bl, false);
                    if (card) collectedGrid.appendChild(card);
                });
                collectedSection.appendChild(collectedGrid);
                grid.appendChild(collectedSection);
            }
        } catch (error) {
            console.error('Load booklists error:', error);
            document.getElementById('booklistGrid').innerHTML = '<p style="color:red;">加载失败: ' + error.message + '</p>';
        }
    }

    function createBooklistCard(bl, isCreated) {
        const card = document.createElement('div');
        card.className = 'booklist-card';
        const name = bl.BooklistName || '未命名';
        const intro = bl.BooklistIntroduction || '';
        const id = bl.BooklistId;
        
        if (!id) {
            console.error('BookList ID is missing!', bl);
            return null;
        }
        
        // 使用字符串拼接避免JSP EL表达式冲突
        let html = '<div>';
        html += '<h3 class="booklist-name">' + escapeHtml(name) + '</h3>';
        html += '<p class="booklist-intro">' + escapeHtml(intro) + '</p>';
        html += '</div>';
        html += '<div class="booklist-actions">';
        html += '<button class="btn" onclick="viewDetails(' + id + ', ' + (isCreated ? 'true' : 'false') + ')">查看</button>';
        if (isCreated) {
            html += '<button class="btn btn-danger" onclick="deleteBooklist(' + id + ')">删除</button>';
        } else {
            html += '<button class="btn" style="background:#ef4444" onclick="cancelCollectBooklist(' + id + ')">取消收藏</button>';
        }
        html += '</div>';
        
        card.innerHTML = html;
        return card;
    }
    
    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    document.getElementById('createBooklistForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('newName').value.trim();
        const intro = document.getElementById('newIntro').value.trim();
        
        if (!name) {
            alert('请输入书单名称');
            return;
        }
        
        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('introduction', intro);

        try {
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: formData
        });
            
            console.log('Create response status:', response.status);
            
            if (!response.ok) {
                const errorText = await response.text();
                console.error('Create error:', errorText);
                alert('创建失败: ' + errorText);
                return;
            }
            
            const result = await response.json();
            console.log('Create success:', result);
        document.getElementById('createBooklistForm').reset();
        loadBooklists();
        } catch (error) {
            console.error('Create booklist error:', error);
            alert('创建失败: ' + error.message);
        }
    });

    async function deleteBooklist(id) {
        if (!confirm('确定要删除这个书单吗？')) return;
        await fetch('<%=request.getContextPath()%>/api/book/booklists/' + id, { method: 'DELETE' });
        loadBooklists();
    }

    let currentBooklistId = null;
    let currentIsCreated = false;
    let createdBooklistIds = new Set();

    async function viewDetails(id, isCreated) {
        if (!id || id === 'undefined' || id === 'null') {
            console.error('Invalid booklist ID:', id);
            alert('无效的书单ID');
            return;
        }
        currentBooklistId = id;
        // 如果传入了isCreated参数，使用它；否则检查是否在创建列表中
        if (isCreated !== undefined) {
            currentIsCreated = isCreated;
        } else {
            currentIsCreated = createdBooklistIds.has(id);
        }
        
        try {
        const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + id);
            if (!response.ok) {
                const errorText = await response.text();
                console.error('API Error:', errorText);
                alert('加载书单详情失败: ' + errorText);
                return;
            }
        const data = await response.json();
        console.log('Booklist details data:', data);
        const modalTitle = document.getElementById('modalTitle');
        const modalBody = document.getElementById('modalBody');
        
            // 数据结构：{ "BooklistInfo": {...}, "Books": [...] }
            const booklist = data.BooklistInfo || {};
            const booklistName = booklist.BooklistName || '未命名';
            modalTitle.textContent = (currentIsCreated ? '编辑: ' : '查看: ') + escapeHtml(booklistName);
            
            // 书单信息编辑区域（仅创建的书单可编辑）
            let headerHtml = '';
            if (currentIsCreated) {
                headerHtml += '<div style="margin-bottom:15px;padding:10px;background:#f3f4f6;border-radius:8px">';
                headerHtml += '<div style="margin-bottom:10px">';
                headerHtml += '<strong>书单名称:</strong> ' + escapeHtml(booklistName);
                headerHtml += ' <button class="btn" style="padding:5px 10px;font-size:12px" onclick="editBooklistName(' + id + ')">编辑</button>';
                headerHtml += '</div>';
                headerHtml += '<div>';
                headerHtml += '<strong>简介:</strong> ' + escapeHtml(booklist.BooklistIntroduction || '无');
                headerHtml += ' <button class="btn" style="padding:5px 10px;font-size:12px" onclick="editBooklistIntro(' + id + ')">编辑</button>';
                headerHtml += '</div>';
                headerHtml += '</div>';
            } else {
                // 收藏的书单显示备注编辑
                // 从当前收藏列表中获取备注
                let currentNotes = '';
                try {
                    const collectedResponse = await fetch(API_MY);
                    if (collectedResponse.ok) {
                        const collectedData = await collectedResponse.json();
                        const collected = collectedData.Collected || [];
                        const collectedBooklist = collected.find(b => b.BooklistId === id);
                        currentNotes = collectedBooklist?.Notes || '';
                    }
                } catch (e) {
                    console.error('Failed to fetch collect notes:', e);
                }
                
                headerHtml += '<div style="margin-bottom:15px;padding:10px;background:#f3f4f6;border-radius:8px">';
                headerHtml += '<div><strong>书单名称:</strong> ' + escapeHtml(booklistName) + '</div>';
                headerHtml += '<div style="margin-top:10px">';
                headerHtml += '<strong>收藏备注:</strong> <span id="collectNotesDisplay">' + escapeHtml(currentNotes || '暂无备注') + '</span>';
                headerHtml += ' <button class="btn" style="padding:5px 10px;font-size:12px" onclick="editCollectNotes(' + id + ')">编辑备注</button>';
                headerHtml += '</div>';
                headerHtml += '</div>';
            }
            
            // 安全地获取书籍列表
            const books = data.Books || [];
            let booksHtml = '<h4>书单中的书籍</h4><ul style="list-style:none;padding:0">';
            if (books.length > 0) {
                books.forEach(book => {
                    const bookTitle = book.Title || '未知书名';
                    const bookIsbn = book.ISBN || '';
                    booksHtml += '<li style="padding:8px;margin:5px 0;background:#f9fafb;border-radius:4px;display:flex;justify-content:space-between;align-items:center">';
                    booksHtml += '<span>' + escapeHtml(bookTitle) + '</span>';
                    if (currentIsCreated) {
                        booksHtml += '<button class="btn btn-danger" style="padding:5px 10px;font-size:12px" onclick="removeBook(' + id + ', \'' + escapeHtml(bookIsbn) + '\')">移除</button>';
                    }
                    booksHtml += '</li>';
            });
        } else {
                booksHtml += '<li style="padding:8px;color:#6b7280">暂无书籍</li>';
        }
        booksHtml += '</ul>';

            let modalHtml = headerHtml + booksHtml;
            if (currentIsCreated) {
                modalHtml += '<hr>';
                modalHtml += '<h4>添加书籍到书单</h4>';
                modalHtml += '<form onsubmit="addBook(event, ' + id + ')">';
                modalHtml += '<input type="text" placeholder="输入书籍ISBN" required style="padding:8px;width:200px;margin-right:10px">';
                modalHtml += '<button type="submit" class="btn">添加</button>';
                modalHtml += '</form>';
            } else {
                modalHtml += '<hr>';
                modalHtml += '<button class="btn btn-danger" onclick="cancelCollectBooklist(' + id + ')">取消收藏</button>';
            }

            modalBody.innerHTML = modalHtml;
        document.getElementById('detailModal').style.display = 'block';
        } catch (error) {
            console.error('View details error:', error);
            alert('加载书单详情失败: ' + error.message);
        }
    }

    function closeModal() {
        document.getElementById('detailModal').style.display = 'none';
    }

    async function addBook(event, booklistId) {
        event.preventDefault();
        const isbn = event.target.querySelector('input').value;
        const formData = new URLSearchParams();
        formData.append('isbn', isbn);
        await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/books', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: formData
        });
        viewDetails(booklistId); // Refresh modal content
    }

    async function removeBook(booklistId, isbn) {
        if (!confirm('确定要从书单中移除此图书吗？')) return;
        try {
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/books/' + encodeURIComponent(isbn), { 
                method: 'DELETE' 
            });
            if (!response.ok) {
                const errorText = await response.text();
                alert('移除失败: ' + errorText);
                return;
            }
            viewDetails(booklistId, currentIsCreated); // Refresh modal content
        } catch (error) {
            console.error('Remove book error:', error);
            alert('移除失败: ' + error.message);
        }
    }

    // 编辑书单名称
    async function editBooklistName(booklistId) {
        const newName = prompt('请输入新书单名称');
        if (!newName || !newName.trim()) return;
        
        try {
            const formData = new URLSearchParams();
            formData.append('NewName', newName.trim());
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/name', {
                method: 'PUT',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            });
            if (!response.ok) {
                const errorText = await response.text();
                alert('更新失败: ' + errorText);
                return;
            }
            viewDetails(booklistId, true);
            loadBooklists();
        } catch (error) {
            console.error('Update name error:', error);
            alert('更新失败: ' + error.message);
        }
    }

    // 编辑书单简介
    async function editBooklistIntro(booklistId) {
        const newIntro = prompt('请输入新简介');
        if (newIntro === null) return;
        
        try {
            const formData = new URLSearchParams();
            formData.append('NewIntro', newIntro || '');
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/intro', {
                method: 'PUT',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            });
            if (!response.ok) {
                const errorText = await response.text();
                alert('更新失败: ' + errorText);
                return;
            }
            viewDetails(booklistId, true);
            loadBooklists();
        } catch (error) {
            console.error('Update intro error:', error);
            alert('更新失败: ' + error.message);
        }
    }

    // 收藏书单
    async function collectBooklist(booklistId) {
        const notes = prompt('请输入收藏备注（可选）') || '';
        try {
            const formData = new URLSearchParams();
            formData.append('notes', notes);
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/collect', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            });
            if (!response.ok) {
                const errorText = await response.text();
                alert('收藏失败: ' + errorText);
                return;
            }
            const result = await response.json();
            if (result.Success === 1) {
                alert('收藏成功！');
                loadBooklists();
                closeRecommendModal();
            } else {
                alert('收藏失败: ' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('Collect error:', error);
            alert('收藏失败: ' + error.message);
        }
    }

    // 取消收藏
    async function cancelCollectBooklist(booklistId) {
        if (!confirm('确定要取消收藏这个书单吗？')) return;
        try {
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/collect', {
                method: 'DELETE'
            });
            if (!response.ok) {
                const errorText = await response.text();
                alert('取消收藏失败: ' + errorText);
                return;
            }
            const result = await response.json();
            if (result.Success === 1) {
                alert('取消收藏成功！');
                closeModal();
                loadBooklists();
            } else {
                alert('取消收藏失败: ' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('Cancel collect error:', error);
            alert('取消收藏失败: ' + error.message);
        }
    }

    // 编辑收藏备注
    async function editCollectNotes(booklistId) {
        const currentNotes = document.getElementById('collectNotesDisplay')?.textContent || '';
        const newNotes = prompt('请输入新的收藏备注', currentNotes === '暂无备注' ? '' : currentNotes);
        if (newNotes === null) return;
        
        try {
            const formData = new URLSearchParams();
            formData.append('NewNotes', newNotes || '');
            const response = await fetch('<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/collect/notes', {
                method: 'PUT',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: formData
            });
            if (!response.ok) {
                const errorText = await response.text();
                alert('更新失败: ' + errorText);
                return;
            }
            const result = await response.json();
            if (result.Success === 1) {
                document.getElementById('collectNotesDisplay').textContent = newNotes || '暂无备注';
            } else {
                alert('更新失败: ' + (result.message || '未知错误'));
            }
        } catch (error) {
            console.error('Update notes error:', error);
            alert('更新失败: ' + error.message);
        }
    }

    // 显示推荐书单
    async function showRecommendModal() {
        try {
            // 先获取一个书单ID用于推荐
            const response = await fetch(API_MY);
            if (!response.ok) {
                alert('无法获取推荐书单');
                return;
            }
            const data = await response.json();
            const created = data.Created || [];
            const collected = data.Collected || [];
            
            let booklistId = null;
            if (created.length > 0) {
                const randomIndex = Math.floor(Math.random() * created.length);
                booklistId = created[randomIndex].BooklistId;
            } else if (collected.length > 0) {
                const randomIndex = Math.floor(Math.random() * collected.length);
                booklistId = collected[randomIndex].BooklistId;
            }
            if (!booklistId) {
                document.getElementById('recommendBody').innerHTML = '<p>暂无推荐书单</p>';
                document.getElementById('recommendModal').style.display = 'block';
                return;
            }
            
            // 获取推荐书单
            const recommendUrl = '<%=request.getContextPath()%>/api/book/booklists/' + booklistId + '/recommend?limit=10';
            const recommendResponse = await fetch(recommendUrl);
            if (!recommendResponse.ok) {
                alert('获取推荐书单失败');
                return;
            }
            const recommendData = await recommendResponse.json();
            const items = recommendData.Items || [];
            
            const recommendBody = document.getElementById('recommendBody');
            if (items.length === 0) {
                recommendBody.innerHTML = '<p>暂无推荐书单</p>';
            } else {
                let html = '<div style="max-height:500px;overflow-y:auto">';
                items.forEach(item => {
                    html += '<div style="padding:15px;margin:10px 0;border:1px solid #e5e7eb;border-radius:8px;display:flex;justify-content:space-between;align-items:center">';
                    html += '<div style="flex:1">';
                    html += '<h3 style="margin:0 0 5px;font-weight:bold">' + escapeHtml(item.BooklistName || '未命名') + '</h3>';
                    html += '<p style="margin:5px 0;color:#6b7280;font-size:14px">' + escapeHtml(item.BooklistIntroduction || '') + '</p>';
                    html += '<p style="margin:5px 0;color:#9ca3af;font-size:12px">共同书籍: ' + (item.MatchingBooksCount || 0) + ' 本</p>';
                    html += '</div>';
                    html += '<button class="btn" onclick="collectBooklist(' + item.BooklistId + ')">收藏</button>';
                    html += '</div>';
                });
                html += '</div>';
                recommendBody.innerHTML = html;
            }
            
            document.getElementById('recommendModal').style.display = 'block';
        } catch (error) {
            console.error('Show recommend error:', error);
            alert('获取推荐书单失败: ' + error.message);
        }
    }

    function closeRecommendModal() {
        document.getElementById('recommendModal').style.display = 'none';
    }
</script>
</body>
</html>