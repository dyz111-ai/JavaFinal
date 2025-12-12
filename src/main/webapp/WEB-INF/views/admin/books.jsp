<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.List"
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>图书管理 - 管理员界面</title>
<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<style>
    .form-input {
        width: 100%;
        padding: 0.5rem;
        border: 1px solid #d1d5db;
        border-radius: 0.375rem;
        transition: all 0.2s;
    }
    
    .form-input:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
    }
    
    .btn-primary {
        background-color: #2563eb;
        color: white;
        font-weight: bold;
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        transition: background-color 0.2s;
        cursor: pointer;
        border: none;
    }
    
    .btn-primary:hover {
        background-color: #1d4ed8;
    }
    
    .btn-secondary {
        background-color: #e5e7eb;
        color: #374151;
        font-weight: bold;
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        transition: background-color 0.2s;
        cursor: pointer;
        border: none;
    }
    
    .btn-secondary:hover {
        background-color: #d1d5db;
    }
    
    .btn-danger {
        background-color: #dc2626;
        color: white;
        font-weight: bold;
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        transition: background-color 0.2s;
        cursor: pointer;
        border: none;
    }
    
    .btn-danger:hover {
        background-color: #b91c1c;
    }
    
    .btn-outline {
        border: 1px solid #3b82f6;
        color: #3b82f6;
        font-weight: bold;
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        transition: all 0.2s;
        cursor: pointer;
        background: white;
    }
    
    .btn-outline:hover {
        background-color: #3b82f6;
        color: white;
    }
    
    .table-container {
        overflow-x: auto;
    }
    
    .book-status-available {
        color: #10b981;
        font-weight: 500;
    }
    
    .book-status-unavailable {
        color: #f59e0b;
        font-weight: 500;
    }
    
    .book-status-archived {
        color: #6b7280;
        font-weight: 500;
    }

    .modal-backdrop {
        background-color: rgba(0, 0, 0, 0.5);
        position: fixed;
        inset: 0;
        align-items: center;
        justify-content: center;
        z-index: 50;
        display: none;
    }

    .modal-backdrop:not(.hidden) {
        display: flex;
    }
    
    .modal {
        background-color: white;
        border-radius: 0.5rem;
        box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        width: 100%;
        max-width: 500px;
        max-height: 90vh;
        overflow-y: auto;
    }
    
    .shadow-sm {
        box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    }
</style>
</head>
<body class="bg-gray-50 min-h-screen">
    <!-- 导入管理员导航栏 -->
    <jsp:include page="navbar.jsp" />
    
    <div class="container mx-auto px-4 py-8">
        <h1 class="text-2xl font-bold mb-6">图书管理系统</h1>
        
        <!-- 新书入库表单 -->
        <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h2 class="text-xl font-bold mb-4">新书入库</h2>
            <form id="add-copy-form" onsubmit="handleAddCopies(); return false;">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <div>
                        <label for="isbn" class="block font-medium mb-1">ISBN</label>
                        <input type="text" id="isbn" class="form-input" required placeholder="请输入图书ISBN">
                    </div>
                    <div>
                        <label for="quantity" class="block font-medium mb-1">入库数量</label>
                        <input type="number" id="quantity" class="form-input" min="1" value="1" required placeholder="请输入入库数量">
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼宇</label>
                        <select id="building" class="form-input" onchange="updateFloors()" required>
                            <option value="">请选择楼宇</option>
                            <option value="1">逸夫图书馆</option>
                            <option value="2">科技分馆</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼层</label>
                        <select id="floor" class="form-input" onchange="updateZones()" required disabled>
                            <option value="">请先选择楼宇</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">区域</label>
                        <select id="zone" class="form-input" required disabled>
                            <option value="">请先选择楼层</option>
                        </select>
                    </div>
                    <div class="md:col-span-2 lg:col-span-4">
                        <button type="submit" class="btn-primary">添加图书副本</button>
                    </div>
                </div>
            </form>
        </div>
        
        <!-- 搜索部分 -->
        <div class="bg-white p-6 rounded-lg shadow-sm mb-6">
            <h2 class="text-xl font-bold mb-4">图书搜索</h2>
            <form id="search-form" class="flex flex-col md:flex-row gap-4" onsubmit="handleSearch(); return false;">
                <input type="text" id="search-text" name="search" placeholder="输入书名、ISBN或作者进行搜索" class="form-input flex-grow">
                <button type="submit" class="btn-primary whitespace-nowrap">搜索图书</button>
                <button type="button" id="btn-show-all" class="btn-secondary whitespace-nowrap" onclick="showAllBooks()">显示全部图书</button>
            </form>
        </div>
        
        <!-- 搜索结果 -->
        <div class="bg-white p-6 rounded-lg shadow-sm">
            <h2 class="text-xl font-bold mb-4">搜索结果</h2>
            <div id="loading-indicator" class="text-center py-8" style="display: none;">加载中...</div>
            <div class="table-container">
                <table class="w-full text-sm text-left">
                    <thead class="text-xs text-gray-700 uppercase bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3">书名</th>
                            <th scope="col" class="px-4 py-3">ISBN</th>
                            <th scope="col" class="px-4 py-3">作者</th>
                            <th scope="col" class="px-4 py-3">条码</th>
                            <th scope="col" class="px-4 py-3">状态</th>
                            <th scope="col" class="px-4 py-3">位置</th>
                            <th scope="col" class="px-4 py-3">操作</th>
                        </tr>
                    </thead>
                    <tbody id="book-results-table">
                        <!-- 结果将通过JavaScript动态填充 -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <!-- 编辑位置弹窗 -->
    <div id="location-modal" class="modal-backdrop hidden">
        <div class="modal p-6">
            <h3 class="text-lg font-bold mb-4">修改书籍位置</h3>
            <input type="hidden" id="edit-book-id" />
            <form id="edit-location-form" onsubmit="saveLocation(); return false;">
                <div class="space-y-4 mb-4">
                    <div>
                        <label class="block font-medium mb-1">楼宇</label>
                        <select id="edit-building" class="form-input" onchange="updateEditFloors()" required>
                            <option value="">请选择楼宇</option>
                            <option value="1">逸夫图书馆</option>
                            <option value="2">科技分馆</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼层</label>
                        <select id="edit-floor" class="form-input" onchange="updateEditZones()" required disabled>
                            <option value="">请先选择楼宇</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">区域</label>
                        <select id="edit-zone" class="form-input" required disabled>
                            <option value="">请先选择楼层</option>
                        </select>
                    </div>
                </div>
                <div class="flex gap-2 justify-end">
                    <button type="button" id="btn-close-location" class="btn-secondary" onclick="closeLocationDialog()">取消</button>
                    <button type="submit" class="btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        let loading = false;
        
        // 页面加载时获取所有图书
        document.addEventListener('DOMContentLoaded', function() {
            showAllBooks();
        });
        
        // 显示所有图书
        async function showAllBooks() {
            try {
                showLoading(true);
                const url = '<%= request.getContextPath() %>/api/admin/books';
                const response = await axios.get(url);
                renderBooks(response.data);
            } catch (error) {
                console.error('获取图书列表失败:', error);
                alert('获取图书列表失败，请重试');
            } finally {
                showLoading(false);
            }
        }
        
        // 搜索图书
        async function handleSearch() {
            const searchText = document.getElementById('search-text').value;
            if (!searchText.trim()) {
                alert('请输入搜索内容');
                return;
            }
            
            try {
                showLoading(true);
                const url = '<%= request.getContextPath() %>/api/admin/books';
                const response = await axios.get(url, {
                    params: { search: searchText.trim() }
                });
                renderBooks(response.data);
            } catch (error) {
                console.error('搜索图书失败:', error);
                alert('搜索失败，请重试');
            } finally {
                showLoading(false);
            }
        }
        
        // 渲染图书列表
        function renderBooks(books) {
            const tableBody = document.getElementById('book-results-table');
            tableBody.innerHTML = '';
            
            if (!books || books.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="7" class="px-4 py-8 text-center text-gray-500">未找到相关图书</td></tr>';
                return;
            }
            
            books.forEach(function(book) {
                console.log('[前端] 渲染图书:', {
                    bookId: book.bookId,
                    title: book.title,
                    location: book.location
                });
                
                const row = document.createElement('tr');
                row.className = 'border-b';
                
                // 直接使用book的状态
                let status = book.status || '未知';
                let statusClass = 'book-status-archived';
                
                if (status === '正常') {
                    statusClass = 'book-status-available';
                } else if (status === '借出') {
                    statusClass = 'book-status-unavailable';
                } else if (status === '下架') {
                    statusClass = 'book-status-archived';
                } 
                
                // 格式化书架位置
                let location = book.location || '-';
                
                row.innerHTML = 
                    '<td class="px-4 py-3 font-medium">' + (book.title || '未知') + '</td>' +
                    '<td class="px-4 py-3">' + (book.isbn || '未知') + '</td>' +
                    '<td class="px-4 py-3">' + (book.author || '未知') + '</td>' +
                    '<td class="px-4 py-3">' + (book.barcode || '-') + '</td>' +
                    '<td class="px-4 py-3"><span class="' + statusClass + '">' + status + '</span></td>' +
                    '<td class="px-4 py-3">' + location + '</td>' +
                    '<td class="px-4 py-3">' +
                        (status != '下架' ?
                            '<div class="flex gap-2">' +
                                '<button type="button" onclick="openLocationDialog(\'' + book.bookId + '\', \'' + location + '\')" class="btn-outline text-xs">修改位置</button>' +
                                '<button type="button" onclick="handleDelete(\'' + book.bookId + '\')" class="btn-danger text-xs">下架</button>' +
                            '</div>' :
                                '<span class="text-gray-500 text-xs">已下架</span>'
                        ) +
                    '</td>';
                
                tableBody.appendChild(row);
            });
        }
        
        // 显示加载指示器
        function showLoading(show) {
            loading = show;
            const indicator = document.getElementById('loading-indicator');
            const tableContainer = document.querySelector('.table-container');
            if (show) {
                indicator.style.display = 'block';
                if (tableContainer) tableContainer.style.display = 'none';
            } else {
                indicator.style.display = 'none';
                if (tableContainer) tableContainer.style.display = 'block';
            }
        }
        
        // 打开位置编辑弹窗
        function openLocationDialog(bookId, location) {
            document.getElementById('edit-book-id').value = bookId;
            
            // 从位置字符串中提取信息（如果location是shelfcode，可能无法解析，需要从后端获取完整信息）
            // 暂时不解析，让用户重新选择
            document.getElementById('edit-building').value = '';
            document.getElementById('edit-floor').value = '';
            document.getElementById('edit-zone').value = '';
            
            document.getElementById('location-modal').classList.remove('hidden');
        }
        
        // 关闭位置编辑弹窗
        function closeLocationDialog() {
            document.getElementById('location-modal').classList.add('hidden');
            document.getElementById('edit-location-form').reset();
        }
        
        // 保存位置修改
        async function saveLocation() {
            const bookId = document.getElementById('edit-book-id').value;
            const building = document.getElementById('edit-building').value;
            const floor = document.getElementById('edit-floor').value;
            const zone = document.getElementById('edit-zone').value;
            
            console.log('[前端] 保存位置修改，参数:', {
                bookId: bookId,
                building: building,
                floor: floor,
                zone: zone
            });
            
            if (!bookId || !building || !floor || !zone) {
                alert('请填写完整的位置信息');
                return;
            }
            
            try {
                const response = await axios.put('<%= request.getContextPath() %>/api/admin/books/' + bookId + '/location', {
                    buildingId: parseInt(building),
                    floor: parseInt(floor),
                    zone: zone
                });
                
                console.log('[前端] 更新位置响应:', response.data);
                
                if (response.data && response.data.success) {
                alert('图书位置更新成功');
                closeLocationDialog();
                    // 延迟一下再刷新，确保数据库已更新
                    setTimeout(function() {
                        showAllBooks();
                    }, 100);
                } else {
                    alert('更新位置失败：' + (response.data.error || '未知错误'));
                }
            } catch (error) {
                console.error('[前端] 更新位置失败:', error);
                console.error('[前端] 错误详情:', error.response);
                alert('更新位置失败：' + (error.response?.data?.error || error.message || '请重试'));
            }
        }
        
        // 处理图书下架
        async function handleDelete(bookId) {
            if (!confirm('确定要下架该图书吗？')) {
                return;
            }
            
            try {
                await axios.put('<%= request.getContextPath() %>/api/admin/books/' + bookId + '/takedown');
                alert('图书下架成功');
                showAllBooks();
            } catch (error) {
                console.error('图书下架失败:', error);
                alert('下架失败，请重试');
            }
        }
        
        // 添加图书副本
        async function handleAddCopies() {
            try {
                const isbn = document.getElementById('isbn').value;
                const quantity = document.getElementById('quantity').value;
                const buildingId = document.getElementById('building').value;
                const floor = document.getElementById('floor').value;
                const zone = document.getElementById('zone').value;
                
                if (!isbn || !quantity || !buildingId || !floor || !zone) {
                    alert('请填写完整信息');
                    return;
                }
                
                await axios.post('<%= request.getContextPath() %>/api/admin/books/copies', {
                    isbn: isbn,
                    numberOfCopies: parseInt(quantity),
                    buildingId: parseInt(buildingId),
                    floor: parseInt(floor),
                    zone: zone
                });
                
                alert('图书副本添加成功');
                document.getElementById('add-copy-form').reset();
                // 重置表单后恢复初始状态
                document.getElementById('floor').disabled = true;
                document.getElementById('zone').disabled = true;
                showAllBooks();
            } catch (error) {
                console.error('添加图书副本失败:', error);
                alert('添加失败，请重试');
            }
        }
        
        // 位置联动更新函数
        function updateFloors() {
            const building = document.getElementById('building').value;
            const floorSelect = document.getElementById('floor');
            const zoneSelect = document.getElementById('zone');
            
            // 清空区域选项
            zoneSelect.innerHTML = '<option value="">请先选择楼层</option>';
            zoneSelect.disabled = true;
            
            if (!building) {
                floorSelect.innerHTML = '<option value="">请先选择楼宇</option>';
                floorSelect.disabled = true;
                return;
            }
            
            // 根据楼宇设置楼层选项
            floorSelect.disabled = false;
            if (building === '1') {
                // 逸夫图书馆：1-6层都可选
                floorSelect.innerHTML = 
                    '<option value="1">1楼</option>' +
                    '<option value="2">2楼</option>' +
                    '<option value="3">3楼</option>' +
                    '<option value="4">4楼</option>' +
                    '<option value="5">5楼</option>' +
                    '<option value="6">6楼</option>';
            } else if (building === '2') {
                // 科技分馆：1-3层都可选
                floorSelect.innerHTML = 
                    '<option value="1">1楼</option>' +
                    '<option value="2">2楼</option>' +
                    '<option value="3">3楼</option>';
            }
            
            // 自动触发区域更新
            updateZones();
        }
        
        function updateZones() {
            const building = document.getElementById('building').value;
            const floor = document.getElementById('floor').value;
            const zoneSelect = document.getElementById('zone');
            
            if (!building || !floor) {
                zoneSelect.innerHTML = '<option value="">请先选择楼层</option>';
                zoneSelect.disabled = true;
                return;
        }
        
            // 根据楼宇设置区域
            zoneSelect.disabled = false;
            if (building === '1') {
                // 逸夫图书馆：A区和B区
                zoneSelect.innerHTML = 
                    '<option value="A">A区</option>' +
                    '<option value="B">B区</option>';
            } else if (building === '2') {
                // 科技分馆：C区和D区
                zoneSelect.innerHTML = 
                    '<option value="C">C区</option>' +
                    '<option value="D">D区</option>';
            }
        }
        
        function updateShelves() {
            // 不再需要
        }
        
        // 编辑模态框中的位置联动函数
        function updateEditFloors() {
            const building = document.getElementById('edit-building').value;
            const floorSelect = document.getElementById('edit-floor');
            const zoneSelect = document.getElementById('edit-zone');
            
            // 清空区域选项
            zoneSelect.innerHTML = '<option value="">请先选择楼层</option>';
            zoneSelect.disabled = true;
            
            if (!building) {
                floorSelect.innerHTML = '<option value="">请先选择楼宇</option>';
                floorSelect.disabled = true;
                return;
            }
            
            // 根据楼宇设置楼层选项
            floorSelect.disabled = false;
            if (building === '1') {
                // 逸夫图书馆：1-6层都可选
                floorSelect.innerHTML = 
                    '<option value="1">1楼</option>' +
                    '<option value="2">2楼</option>' +
                    '<option value="3">3楼</option>' +
                    '<option value="4">4楼</option>' +
                    '<option value="5">5楼</option>' +
                    '<option value="6">6楼</option>';
            } else if (building === '2') {
                // 科技分馆：1-3层都可选
                floorSelect.innerHTML = 
                    '<option value="1">1楼</option>' +
                    '<option value="2">2楼</option>' +
                    '<option value="3">3楼</option>';
            }
            
            // 自动触发区域更新
            updateEditZones();
        }
        
        function updateEditZones() {
            const building = document.getElementById('edit-building').value;
            const floor = document.getElementById('edit-floor').value;
            const zoneSelect = document.getElementById('edit-zone');
            
            if (!building || !floor) {
                zoneSelect.innerHTML = '<option value="">请先选择楼层</option>';
                zoneSelect.disabled = true;
                return;
            }
            
            // 根据楼宇设置区域
            zoneSelect.disabled = false;
            if (building === '1') {
                // 逸夫图书馆：A区和B区
                zoneSelect.innerHTML = 
                    '<option value="A">A区</option>' +
                    '<option value="B">B区</option>';
            } else if (building === '2') {
                // 科技分馆：C区和D区
                zoneSelect.innerHTML = 
                    '<option value="C">C区</option>' +
                    '<option value="D">D区</option>';
                }
        }
        
        function updateEditShelves() {
            // 不再需要
        }
    </script>
</body>
</html>
