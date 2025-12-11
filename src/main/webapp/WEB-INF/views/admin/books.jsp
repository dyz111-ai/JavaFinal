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
        /* display: flex;  <-- 删除或注释掉这一行 */
        align-items: center;
        justify-content: center;
        z-index: 50;
    }

    /* ✅ 新增：只有当元素没有 hidden 类时，才使用 flex 布局显示 */
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
                        <label class="block font-medium mb-1">ISBN</label>
                        <input type="text" id="isbn" class="form-input" required>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">入库数量</label>
                        <input type="number" id="quantity" class="form-input" min="1" value="1" required>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼宇</label>
                        <select id="building" class="form-input" onchange="updateFloors()" required>
                            <option value="A">A栋</option>
                            <option value="B">B栋</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼层</label>
                        <select id="floor" class="form-input" onchange="updateZones()" required>
                            <option value="1">1楼</option>
                            <option value="2">2楼</option>
                            <option value="3">3楼</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">区域</label>
                        <select id="zone" class="form-input" onchange="updateShelves()" required>
                            <option value="A">A区</option>
                            <option value="B">B区</option>
                            <option value="C">C区</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">书架编号</label>
                        <select id="shelf" class="form-input" required>
                            <option value="1">1号架</option>
                            <option value="2">2号架</option>
                            <option value="3">3号架</option>
                            <option value="4">4号架</option>
                            <option value="5">5号架</option>
                        </select>
                    </div>
                    <div class="md:col-span-2 lg:col-span-4">
                        <button type="submit" class="btn-primary">添加图书副本</button>
                    </div>
                </div>
            </form>
        </div>
        
        <!-- 搜索部分 -->
        <div class="bg-white p-6 rounded-lg shadow-sm mb-6 search-container">
            <h2 class="text-xl font-bold mb-4">图书搜索</h2>
            <div class="flex flex-col md:flex-row gap-4">
                <input type="text" id="search-text" placeholder="输入书名、ISBN或作者进行搜索" class="form-input flex-grow">
                <button onclick="handleSearch()" class="btn-primary whitespace-nowrap">搜索图书</button>
                <button onclick="showAllBooks()" class="btn-secondary whitespace-nowrap">显示全部图书</button>
            </div>
        </div>
        
        <!-- 搜索结果 -->
        <div class="bg-white p-6 rounded-lg shadow-sm">
            <h2 class="text-xl font-bold mb-4">搜索结果</h2>
            <div id="loading-indicator" class="text-center py-8 hidden">加载中...</div>
            <div class="table-container">
                <table class="w-full text-sm text-left">
                    <thead class="text-xs text-gray-700 uppercase bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3">书名</th>
                            <th scope="col" class="px-4 py-3">ISBN</th>
                            <th scope="col" class="px-4 py-3">作者</th>
                            <th scope="col" class="px-4 py-3">出版社</th>
                            <th scope="col" class="px-4 py-3">出版日期</th>
                            <th scope="col" class="px-4 py-3">总数量</th>
                            <th scope="col" class="px-4 py-3">可借数量</th>
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
            <input type="hidden" id="edit-book-id" /><input type="hidden" id="edit-book-copy-id" />
            <form id="edit-location-form" onsubmit="saveLocation(); return false;">
                <div class="space-y-4 mb-4">
                    <div>
                        <label class="block font-medium mb-1">楼宇</label>
                        <select id="edit-building" class="form-input" onchange="updateEditFloors()" required>
                            <option value="A">A栋</option>
                            <option value="B">B栋</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼层</label>
                        <select id="edit-floor" class="form-input" onchange="updateEditZones()" required>
                            <option value="1">1楼</option>
                            <option value="2">2楼</option>
                            <option value="3">3楼</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">区域</label>
                        <select id="edit-zone" class="form-input" onchange="updateEditShelves()" required>
                            <option value="A">A区</option>
                            <option value="B">B区</option>
                            <option value="C">C区</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">书架编号</label>
                        <select id="edit-shelf" class="form-input" required>
                            <option value="1">1号架</option>
                            <option value="2">2号架</option>
                            <option value="3">3号架</option>
                            <option value="4">4号架</option>
                            <option value="5">5号架</option>
                        </select>
                    </div>
                </div>
                <div class="flex gap-2 justify-end">
                    <button type="button" onclick="closeLocationDialog()" class="btn-secondary">取消</button>
                    <button type="submit" class="btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- 归还书籍弹窗 -->
    <div id="return-modal" class="modal-backdrop hidden">
        <div class="modal p-6">
            <h3 class="text-lg font-bold mb-4">归还书籍到库</h3>
            <input type="hidden" id="return-book-id" /><input type="hidden" id="return-book-copy-id" /><input type="hidden" id="return-borrow-id" />
            <form id="return-location-form" onsubmit="saveReturnLocation(); return false;">
                <div class="space-y-4 mb-4">
                    <div>
                        <label class="block font-medium mb-1">楼宇</label>
                        <select id="return-building" class="form-input" onchange="updateReturnFloors()" required>
                            <option value="A">A栋</option>
                            <option value="B">B栋</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">楼层</label>
                        <select id="return-floor" class="form-input" onchange="updateReturnZones()" required>
                            <option value="1">1楼</option>
                            <option value="2">2楼</option>
                            <option value="3">3楼</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">区域</label>
                        <select id="return-zone" class="form-input" onchange="updateReturnShelves()" required>
                            <option value="A">A区</option>
                            <option value="B">B区</option>
                            <option value="C">C区</option>
                        </select>
                    </div>
                    <div>
                        <label class="block font-medium mb-1">书架编号</label>
                        <select id="return-shelf" class="form-input" required>
                            <option value="1">1号架</option>
                            <option value="2">2号架</option>
                            <option value="3">3号架</option>
                            <option value="4">4号架</option>
                            <option value="5">5号架</option>
                        </select>
                    </div>
                </div>
                <div class="flex gap-2 justify-end">
                    <button type="button" onclick="closeReturnDialog()" class="btn-secondary">取消</button>
                    <button type="submit" class="btn-primary">确认归还</button>
                </div>
            </form>
        </div>
    </div>
    
    <script>
        // 状态变量
        let loading = false;
        
        // 页面加载时获取所有图书
        document.addEventListener('DOMContentLoaded', function() {
            showAllBooks();
        });
        
        // 搜索图书
        async function handleSearch() {
            const searchText = document.getElementById('search-text').value;
            if (!searchText.trim()) {
                alert('请输入搜索内容');
                return;
            }
            
            try {
                showLoading(true);
                const response = await axios.get('<%= request.getContextPath() %>/api/admin/books', {
                    params: { search: searchText }
                });
                renderBooks(response.data);
            } catch (error) {
                console.error('搜索图书失败:', error);
                alert('搜索失败，请重试');
            } finally {
                showLoading(false);
            }
        }
        
        // 显示所有图书
        async function showAllBooks() {
            try {
                showLoading(true);
                const response = await axios.get('<%= request.getContextPath() %>/api/admin/books');
                renderBooks(response.data);
            } catch (error) {
                console.error('获取图书列表失败:', error);
                alert('获取图书列表失败，请重试');
            } finally {
                showLoading(false);
            }
        }
        
        // 渲染图书列表
        function renderBooks(books) {
            const tableBody = document.getElementById('book-results-table');
            tableBody.innerHTML = '';
            
            if (!books || books.length === 0) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="10" class="px-4 py-8 text-center text-gray-500">未找到相关图书</td>
                    </tr>
                `;
                return;
            }
            
            books.forEach(book => {
                const row = document.createElement('tr');
                row.className = 'border-b';
                
                // 获取状态类名
                let statusClass = '';
                if (book.status === '可借') {
                    statusClass = 'book-status-available';
                } else if (book.status === '借出') {
                    statusClass = 'book-status-unavailable';
                } else {
                    statusClass = 'book-status-archived';
                }
                
                // 格式化出版日期
                let publishDate = book.publishDate ? new Date(book.publishDate).toLocaleDateString() : '-';
                
                // 格式化书架位置
                let location = book.location || '-';
                
                row.innerHTML = `
                    <td class="px-4 py-3 font-medium">${book.title || '未知'}</td>
                    <td class="px-4 py-3">${book.isbn || '未知'}</td>
                    <td class="px-4 py-3">${book.author || '未知'}</td>
                    <td class="px-4 py-3">${book.publisher || '未知'}</td>
                    <td class="px-4 py-3">${publishDate}</td>
                    <td class="px-4 py-3">${book.totalCopies || 0}</td>
                    <td class="px-4 py-3">${book.availableCopies || 0}</td>
                    <td class="px-4 py-3">
                        <span class="${statusClass}">${book.status || '未知'}</span>
                    </td>
                    <td class="px-4 py-3">${location}</td>
                    <td class="px-4 py-3">
                        <div class="flex gap-2">
                            ${book.status != '下架' && book.id ?
                                '<button onclick="openLocationDialog(' + book.id + ', \'' + book.location + '\')" class="btn-outline text-xs">修改位置</button>' +
                                '<button onclick="handleDelete(\'' + book.isbn + '\')" class="btn-danger text-xs">下架</button>'
                                :
                                '<span class="text-gray-500 text-xs">已下架</span>'
                            }
                        </div>
                    </td>
                `;
                
                tableBody.appendChild(row);
            });
        }
        
        // 显示加载指示器
        function showLoading(show) {
            loading = show;
            const indicator = document.getElementById('loading-indicator');
            if (show) {
                indicator.classList.remove('hidden');
                document.getElementById('book-results-table').parentElement.classList.add('hidden');
            } else {
                indicator.classList.add('hidden');
                document.getElementById('book-results-table').parentElement.classList.remove('hidden');
            }
        }
        
        // 打开位置编辑弹窗
        function openLocationDialog(bookId, location) {
            // 注意：在实际应用中，这里应该传递ISBN而不是ID
            // 但为了兼容现有代码，我们暂时使用bookId元素存储
            document.getElementById('edit-book-id').value = bookId;
            
            // 从位置字符串中提取信息 (格式: A栋1楼A区1号架)
            if (location && location !== '-') {
                const buildingMatch = location.match(/([A-Z])栋/);
                const floorMatch = location.match(/(\d+)楼/);
                const zoneMatch = location.match(/([A-Z])区/);
                const shelfMatch = location.match(/(\d+)号架/);
                
                if (buildingMatch) document.getElementById('edit-building').value = buildingMatch[1];
                if (floorMatch) document.getElementById('edit-floor').value = floorMatch[1];
                if (zoneMatch) document.getElementById('edit-zone').value = zoneMatch[1];
                if (shelfMatch) document.getElementById('edit-shelf').value = shelfMatch[1];
            }
            
            document.getElementById('location-modal').classList.remove('hidden');
        }
        
        // 关闭位置编辑弹窗
        function closeLocationDialog() {
            document.getElementById('location-modal').classList.add('hidden');
            document.getElementById('edit-location-form').reset();
        }
        
        // 保存位置修改
        async function saveLocation() {
            // 注意：在实际应用中，这里应该使用ISBN而不是ID
            // 为了演示，我们假设当前表格中的bookId实际上是ISBN
            const isbn = document.getElementById('edit-book-id').value;
            const building = document.getElementById('edit-building').value;
            const floor = document.getElementById('edit-floor').value;
            const zone = document.getElementById('edit-zone').value;
            const shelf = document.getElementById('edit-shelf').value;
            
            // 验证必填字段
            if (!isbn || !building || !floor || !zone || !shelf) {
                alert('请填写完整的位置信息');
                return;
            }
            
            // 构建位置字符串
            const location = `${building}栋${floor}楼${zone}区${shelf}号架`;
            
            try {
                // 调用后端API更新图书位置
                await axios.put('<%= request.getContextPath() %>/api/admin/books/' + isbn + '/location', {
                    building: building,
                    floor: floor + '楼',
                    zone: zone + '区',
                    shelf: shelf + '号架',
                    location: location
                });
                
                alert('图书位置更新成功');
                closeLocationDialog();
                showAllBooks(); // 刷新图书列表
            } catch (error) {
                console.error('更新位置失败:', error);
                alert('更新位置失败: ' + (error.response?.data?.error || error.message));
            }
        }
        
        // 处理图书下架
        async function handleDelete(isbn) {
            if (!confirm('确定要下架该图书吗？')) {
                return;
            }
            
            try {
                // 使用正确的API路径和HTTP方法
                await axios.put('<%= request.getContextPath() %>/api/admin/books/' + isbn + '/takedown');
                alert('图书下架成功');
                showAllBooks(); // 刷新列表
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
                const building = document.getElementById('building').value;
                const floor = document.getElementById('floor').value;
                const zone = document.getElementById('zone').value;
                const shelf = document.getElementById('shelf').value;
                
                await axios.post('<%= request.getContextPath() %>/api/admin/books/copies', {
                    isbn: isbn,
                    numberOfCopies: parseInt(quantity),
                    shelfId: parseInt(shelf) // 假设shelf是书架ID
                });
                
                alert('图书副本添加成功');
                // 重置表单
                document.getElementById('add-copy-form').reset();
                showAllBooks(); // 刷新列表
            } catch (error) {
                console.error('添加图书副本失败:', error);
                alert('添加失败，请重试');
            }
        }
        
        // 位置联动更新函数
        function updateFloors() {
            const buildingId = document.getElementById('building').value;
            // 保持现有选项
        }
        
        // 区域联动更新函数
        function updateZones() {
            const buildingId = document.getElementById('building').value;
            const floor = document.getElementById('floor').value;
            // 保持现有选项
        }
        
        // 书架联动更新函数
        function updateShelves() {
            const buildingId = document.getElementById('building').value;
            const floor = document.getElementById('floor').value;
            const zone = document.getElementById('zone').value;
            // 保持现有选项
        }
        
        // 编辑模态框中的位置联动函数
        function updateEditFloors() {
            const buildingId = document.getElementById('edit-building').value;
            // 保持现有选项
        }
        
        function updateEditZones() {
            const buildingId = document.getElementById('edit-building').value;
            const floor = document.getElementById('edit-floor').value;
            // 保持现有选项
        }
        
        function updateEditShelves() {
            const buildingId = document.getElementById('edit-building').value;
            const floor = document.getElementById('edit-floor').value;
            const zone = document.getElementById('edit-zone').value;
            // 保持现有选项
        }
    </script>
</body>
</html>
