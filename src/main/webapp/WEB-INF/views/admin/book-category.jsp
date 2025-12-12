<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>书籍分类绑定 - 管理员界面</title>
<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<style>
    .form-input { width: 100%; padding: 0.5rem; border: 1px solid #d1d5db; border-radius: 0.375rem; transition: all 0.2s; }
    .form-input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2); }
    .btn-primary { background-color: #2563eb; color: white; font-weight: bold; padding: 0.5rem 1rem; border-radius: 0.375rem; transition: background-color 0.2s; }
    .btn-primary:hover { background-color: #1d4ed8; }
    .btn-secondary { background-color: #e5e7eb; color: #374151; font-weight: bold; padding: 0.5rem 1rem; border-radius: 0.375rem; transition: background-color 0.2s; }
    .btn-secondary:hover { background-color: #d1d5db; }
    .card { background-color: rgba(255,255,255,0.9); backdrop-filter: blur(4px); border-radius: 0.5rem; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05); }
    .book-item { padding: 0.75rem; border: 1px solid #e5e7eb; border-radius: 0.375rem; cursor: pointer; transition: all 0.2s; }
    .book-item:hover { border-color: #3b82f6; background: #eff6ff; }
    .book-item.active { border-color: #2563eb; background: #dbeafe; }
    .category-checkbox { display: flex; align-items: center; gap: 0.5rem; padding: 0.35rem 0; }
    .tag { display: inline-block; padding: 2px 8px; background: #eef2ff; color: #4338ca; border-radius: 9999px; margin-right: 6px; font-size: 12px; }
</style>
</head>
<body class="bg-gray-50 min-h-screen">
    <jsp:include page="navbar.jsp" />
    
    <div class="container mx-auto px-4 py-8">
        <h1 class="text-2xl font-bold mb-2 text-gray-900">书籍分类绑定</h1>
        <p class="text-gray-600 mb-6">搜索图书并为其绑定叶子分类</p>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- 左侧：搜索与列表 -->
            <div class="lg:col-span-1 card p-4">
                <div class="flex gap-2 mb-3">
                    <input type="text" id="search-input" class="form-input" placeholder="输入书名/作者/ISBN 后回车或搜索">
                    <button class="btn-primary" onclick="searchBooks()">搜索</button>
                </div>
                <div id="books-loading" class="text-center text-gray-500 py-4 hidden">加载中...</div>
                <div id="books-empty" class="text-center text-gray-500 py-4 hidden">未找到相关图书</div>
                <div id="books-list" class="space-y-2 max-h-[70vh] overflow-y-auto"></div>
            </div>

            <!-- 右侧：分类绑定 -->
            <div class="lg:col-span-2 card p-4">
                <h2 class="text-xl font-bold mb-2 text-gray-900" id="bind-title">请选择左侧图书</h2>
                <div class="text-gray-600 mb-4" id="bind-subtitle">选中图书后可绑定分类</div>

                <div id="category-section" class="hidden">
                    <div class="mb-3 flex items-center gap-3 text-sm text-gray-700">
                        <span id="book-info"></span>
                        <span id="book-categories" class="text-gray-500"></span>
                    </div>

                    <div id="cats-loading" class="text-center text-gray-500 py-4 hidden">分类加载中...</div>
                    <div id="cats-error" class="text-center text-red-500 py-4 hidden"></div>
                    <div id="cats-container" class="grid grid-cols-1 md:grid-cols-2 gap-2 max-h-[55vh] overflow-y-auto"></div>

                    <div class="text-right mt-4">
                        <button class="btn-primary" onclick="bindCategories()">保存绑定</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

<script>
    let books = [];
    let selectedIsbn = null;
    let leafCategories = [];
    let selectedCategories = [];

    // 文本兜底工具：优先返回非空字符串，否则返回 '-'
    function safeText() {
        for (let i = 0; i < arguments.length; i++) {
            const v = arguments[i];
            if (typeof v === 'string' && v.trim()) return v.trim();
            if (v !== null && v !== undefined && typeof v !== 'boolean') {
                const s = String(v).trim();
                if (s) return s;
            }
        }
        return '-';
    }

    document.getElementById('search-input').addEventListener('keyup', function(e) {
        if (e.key === 'Enter') {
            searchBooks();
        }
    });

    async function searchBooks() {
        const kw = document.getElementById('search-input').value.trim();
        document.getElementById('books-loading').classList.remove('hidden');
        document.getElementById('books-empty').classList.add('hidden');
        document.getElementById('books-list').innerHTML = '';
        try {
            const res = await axios.get('<%= request.getContextPath() %>/api/admin/book-category/books', { params: { search: kw }});
            books = res.data || [];
            renderBooks();
        } catch (e) {
            console.error('搜索图书失败', e);
            document.getElementById('books-empty').classList.remove('hidden');
        } finally {
            document.getElementById('books-loading').classList.add('hidden');
        }
    }

    function renderBooks() {
        const list = document.getElementById('books-list');
        list.innerHTML = '';
        if (!books || books.length === 0) {
            document.getElementById('books-empty').classList.remove('hidden');
            return;
        }
        document.getElementById('books-empty').classList.add('hidden');
        console.log('[前端] 渲染图书列表，数量:', books.length, '示例:', books[0]);
        books.forEach(b => {
            // 兼容字段名大小写，并做兜底
            const isbn = safeText(b.ISBN, b.isbn, '未知ISBN');
            const title = safeText(b.Title, b.title,  '未知标题');
            const author = safeText(b.Author, b.author, '未知作者');
            const catArr = b.Categories || b.categories || [];
            const catStr = (catArr && catArr.length) ? catArr.join('，') : '未绑定分类';
            console.log('[前端] 渲染单本:', { isbn, title, author, catArr });

            const div = document.createElement('div');
            div.className = 'book-item' + (selectedIsbn === isbn ? ' active' : '');
            div.onclick = () => selectBook(b);
            // 使用 createElement 避免模板误差
            const t1 = document.createElement('div');
            t1.className = 'font-semibold text-gray-900';
            t1.textContent = title;
            const t2 = document.createElement('div');
            t2.className = 'text-sm text-gray-600';
            t2.textContent = author + ' · ' + isbn;
            const t3 = document.createElement('div');
            t3.className = 'text-xs text-gray-500 mt-1';
            t3.textContent = catStr;
            div.appendChild(t1);
            div.appendChild(t2);
            div.appendChild(t3);
            list.appendChild(div);
        });
    }

    async function selectBook(book) {
        console.log('[前端] 选择图书，完整对象:', book);
        
        // 后端字段是大写：ISBN, Title, Author, Categories
        const isbn = safeText(book.ISBN, book.isbn, '未知ISBN');
        const title = safeText(book.Title, book.title, isbn, '未知标题');
        const author = safeText(book.Author, book.author, '未知作者');
        const catArr = book.Categories || book.categories || [];
        
        console.log('[前端] 提取的值 - isbn:', isbn, ', title:', title, ', author:', author);
        
        selectedIsbn = isbn;
        selectedCategories = [];
        const section = document.getElementById('category-section');
        const titleEl = document.getElementById('bind-title');
        const subtitleEl = document.getElementById('bind-subtitle');
        const infoEl = document.getElementById('book-info');
        const catsEl = document.getElementById('book-categories');

        section.classList.remove('hidden');
        section.style.display = 'block'; // 防止样式覆盖

        if (titleEl) titleEl.textContent = '为《' + title + '》绑定分类';
        if (subtitleEl) subtitleEl.textContent = 'ISBN: ' + isbn;
        if (infoEl) infoEl.textContent = title + ' / ' + author + ' / ' + isbn;
        if (catsEl) catsEl.textContent = (catArr && catArr.length) ? ('已绑定：' + catArr.join('，')) : '已绑定：无';

        console.log('[前端] 右侧面板赋值:', {
            title: titleEl ? titleEl.textContent : null,
            subtitle: subtitleEl ? subtitleEl.textContent : null,
            info: infoEl ? infoEl.textContent : null,
            cats: catsEl ? catsEl.textContent : null
        });
        renderBooks();

        await Promise.all([loadLeafCategories(), loadBookCategories(isbn)]);
        renderCategories();
    }

    async function loadLeafCategories() {
        try {
            document.getElementById('cats-loading').classList.remove('hidden');
            document.getElementById('cats-error').classList.add('hidden');
            const res = await axios.get('<%= request.getContextPath() %>/api/admin/book-category/leaf-categories');
            leafCategories = res.data || [];
            console.log('[前端] 叶子分类响应:', res.data);
            if (leafCategories.length > 0) {
                console.log('[前端] 叶子分类示例:', leafCategories[0]);
            }
        } catch (e) {
            console.error('加载叶子分类失败', e);
            document.getElementById('cats-error').classList.remove('hidden');
            document.getElementById('cats-error').textContent = '加载分类失败';
        } finally {
            document.getElementById('cats-loading').classList.add('hidden');
        }
    }

    async function loadBookCategories(isbn) {
        try {
            console.log('[前端] 加载图书分类，ISBN:', isbn);
            const res = await axios.get('<%= request.getContextPath() %>/api/admin/book-category/book/' + isbn);
            console.log('[前端] 响应数据:', res.data);
            const list = res.data || [];
            console.log('[前端] 图书已绑定分类列表:', list);
            
            // 兼容多种字段名格式
            selectedCategories = list.map(i => {
                const cid = i.categoryId || i.CategoryID || i.categoryid;
                console.log('[前端] 分类项:', i, '提取的ID:', cid);
                return cid;
            }).filter(Boolean);
            
            console.log('[前端] 选中的分类ID列表:', selectedCategories);
        } catch (e) {
            console.error('[前端] 加载图书分类失败:', e);
            console.error('[前端] 错误详情:', e.response);
            selectedCategories = [];
        }
    }

    function renderCategories() {
        const box = document.getElementById('cats-container');
        box.innerHTML = '';
        if (!leafCategories || leafCategories.length === 0) {
            box.innerHTML = '<div class="text-gray-500">暂无分类，请先在分类管理中新建</div>';
            return;
        }
        console.log('[前端] 渲染分类，总数:', leafCategories.length);
        console.log('[前端] 当前选中的分类ID:', selectedCategories);
        leafCategories.forEach(cat => {
            // 后端返回字段是大写（CategoryID/CategoryName/CategoryPath），兼容小写
            const cid = cat.CategoryID || cat.categoryId || cat.categoryid || '';
            const cname = cat.CategoryName || cat.categoryName || cat.categoryname || '';
            const parentPath = cat.CategoryPath || cat.categoryPath || cat.categorypath || '';
            
            // 检查是否选中
            const isChecked = selectedCategories.includes(cid);
            console.log('[前端] 分类:', cid, cname, '是否选中:', isChecked, 'selectedCategories:', selectedCategories);
            
            const checked = isChecked ? 'checked' : '';
            const div = document.createElement('label');
            div.className = 'category-checkbox';
            const pathText = parentPath ? parentPath + ' / ' : '';
            div.innerHTML = '<input type="checkbox" value="' + cid + '" ' + checked +
                ' onchange="toggleCat(\'' + cid + '\', this.checked)">' +
                ' <span>' + pathText + cname + ' (' + cid + ')</span>';
            box.appendChild(div);
        });
    }

    function toggleCat(cid, checked) {
        if (checked) {
            if (!selectedCategories.includes(cid)) selectedCategories.push(cid);
        } else {
            selectedCategories = selectedCategories.filter(id => id !== cid);
        }
    }

    async function bindCategories() {
        if (!selectedIsbn) {
            alert('请先选择图书');
            return;
        }
        try {
            const res = await axios.post('<%= request.getContextPath() %>/api/admin/book-category/bind', {
                isbn: selectedIsbn,
                categoryIds: selectedCategories
            });
            if (res.data && res.data.success) {
                alert('绑定成功');
                // 更新左侧列表显示
                searchBooks();
            } else {
                alert('绑定失败');
            }
        } catch (e) {
            console.error('绑定失败', e);
            alert('绑定失败：' + (e.response?.data?.error || e.message));
        }
    }

    // 初始加载
    searchBooks();
</script>
</body>
</html>

