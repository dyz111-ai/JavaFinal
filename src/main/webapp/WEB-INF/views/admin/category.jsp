<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>分类管理 - 管理员界面</title>
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
        border: none;
        cursor: pointer;
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
        border: none;
        cursor: pointer;
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
        border: none;
        cursor: pointer;
    }
    
    .btn-danger:hover {
        background-color: #b91c1c;
    }
    
    .btn-action {
        color: #2563eb;
        background: none;
        border: none;
        cursor: pointer;
        padding: 0.25rem 0.5rem;
        font-size: 0.875rem;
        transition: all 0.2s;
    }
    
    .btn-action:hover {
        text-decoration: underline;
    }
    
    .btn-action-delete {
        color: #dc2626;
    }
    
    .category-tree {
        padding: 1rem;
    }
    
    .category-node {
        margin: 0.5rem 0;
        padding: 0.5rem;
        border-left: 2px solid #e5e7eb;
        padding-left: 1rem;
    }
    
    .category-node:hover {
        background-color: #f9fafb;
    }
    
    .category-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        padding: 0.5rem;
    }
    
    .category-name {
        font-weight: 500;
        color: #1f2937;
    }
    
    .category-id {
        font-size: 0.75rem;
        color: #6b7280;
    }
    
    .category-children {
        margin-left: 1.5rem;
        margin-top: 0.5rem;
    }
    
    .modal-backdrop {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 1000;
    }
    
    .modal {
        background: white;
        border-radius: 0.5rem;
        padding: 1.5rem;
        max-width: 500px;
        width: 90%;
        max-height: 90vh;
        overflow-y: auto;
    }
    
    /* 通用隐藏样式，确保初始弹窗不显示 */
    .hidden {
        display: none;
    }

    .modal.hidden {
        display: none;
    }
</style>
</head>
<body class="bg-gray-50 min-h-screen">
    <jsp:include page="navbar.jsp" />
    
    <div class="container mx-auto px-4 py-8">
        <div class="bg-white rounded-lg shadow-sm p-6">
            <h1 class="text-2xl font-bold mb-2 text-gray-900">分类管理</h1>
            <p class="text-gray-600 mb-6">管理图书分类的层级结构，支持添加、编辑、删除分类</p>
            
            <div class="mb-4">
                <button onclick="openAddModal(null)" class="btn-primary">+ 添加顶级分类</button>
            </div>
            
            <div id="category-tree" class="category-tree">
                <div class="text-center text-gray-500 py-8">加载中...</div>
            </div>
        </div>
    </div>
    
    <!-- 添加/编辑分类弹窗 -->
    <div id="category-modal" class="modal-backdrop hidden">
        <div class="modal">
            <h3 class="text-lg font-bold mb-4" id="modal-title">添加分类</h3>
            <form id="category-form" onsubmit="saveCategory(); return false;">
                <input type="hidden" id="edit-category-id" />
                <input type="hidden" id="parent-category-id" />
                
                <div class="mb-4">
                    <label class="block font-medium mb-1">分类ID</label>
                    <input type="text" id="category-id" class="form-input" required 
                           placeholder="请输入分类ID（如：A01）" />
                </div>
                
                <div class="mb-4">
                    <label class="block font-medium mb-1">分类名称</label>
                    <input type="text" id="category-name" class="form-input" required 
                           placeholder="请输入分类名称" />
                </div>
                
                <div class="mb-4 hidden" id="parent-info-block">
                    <label class="block font-medium mb-1">父分类</label>
                    <div class="px-3 py-2 bg-gray-100 rounded border text-gray-700" id="parent-info-text">无（顶级分类）</div>
                </div>
                
                <div class="flex gap-2 justify-end">
                    <button type="button" onclick="closeModal()" class="btn-secondary">取消</button>
                    <button type="submit" class="btn-primary">保存</button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- 删除确认弹窗 -->
    <div id="delete-modal" class="modal-backdrop hidden">
        <div class="modal">
            <h3 class="text-lg font-semibold text-red-600 mb-4">确认删除</h3>
            <p class="text-gray-700 mb-4">
                确定要删除分类 "<span id="delete-category-name"></span>" 吗？
            </p>
            <p class="text-sm text-red-600 mb-4">
                注意：删除后无法恢复，且该分类下的所有子分类和关联图书将受到影响。
            </p>
            <div class="flex gap-2 justify-end">
                <button onclick="closeDeleteModal()" class="btn-secondary">取消</button>
                <button onclick="confirmDelete()" class="btn-danger">确认删除</button>
            </div>
        </div>
    </div>
    
    <script>
        let categories = [];
        let allCategoriesFlat = [];
        let editingCategory = null;
        let deletingCategory = null;
        
        // 页面加载时获取分类树
        window.onload = function() {
            loadCategoryTree();
        };
        
        // 加载分类树
        async function loadCategoryTree() {
            try {
                console.log('[前端] 开始加载分类树');
                const response = await axios.get('<%= request.getContextPath() %>/api/admin/category/tree');
                console.log('[前端] 完整响应对象:', response);
                console.log('[前端] response.data:', response.data);
                console.log('[前端] response.data类型:', typeof response.data);
                console.log('[前端] response.data是否为数组:', Array.isArray(response.data));
                console.log('[前端] response.data值:', JSON.stringify(response.data));
                
                let data = response.data;

                // 如果返回空字符串，按空数组处理
                if (data === '' || data === null || data === undefined) {
                    console.warn('[前端] response.data 为空，按空数组处理');
                    data = [];
                }
                
                // 如果数据是字符串，尝试解析
                if (typeof data === 'string') {
                    console.log('[前端] 数据是字符串，尝试解析JSON');
                    try {
                        data = JSON.parse(data);
                        console.log('[前端] 解析后的数据:', data);
                    } catch (e) {
                        console.error('[前端] JSON解析失败:', e);
                    }
                }
                
                // 确保是数组
                if (!Array.isArray(data)) {
                    console.error('[前端] 响应数据不是数组:', data);
                    console.error('[前端] 数据类型:', typeof data);
                    console.error('[前端] 数据内容:', data);
                    document.getElementById('category-tree').innerHTML = 
                        '<div class="text-center text-red-500 py-8">数据格式错误：期望数组，实际类型：' + typeof data + '</div>';
                    return;
                }
                
                categories = data;
                allCategoriesFlat = flattenCategories(categories);
                console.log('[前端] 分类树加载成功，根节点数量:', categories.length);
                console.log('[前端] 扁平化后总数:', allCategoriesFlat.length);
                renderCategoryTree();
            } catch (error) {
                console.error('[前端] 加载分类树失败:', error);
                console.error('[前端] 错误详情:', error.response);
                console.error('[前端] 错误堆栈:', error.stack);
                document.getElementById('category-tree').innerHTML = 
                    '<div class="text-center text-red-500 py-8">加载失败：' + (error.response?.data?.error || error.message) + '</div>';
            }
        }
        
        // 扁平化分类列表
        function flattenCategories(cats) {
            if (!Array.isArray(cats)) {
                console.error('[前端] flattenCategories: 参数不是数组', cats);
                return [];
            }
            
            let result = [];
            cats.forEach(cat => {
                result.push(cat);
                // 兼容两种字段名格式
                const children = cat.children || cat.Children;
                if (children && Array.isArray(children) && children.length > 0) {
                    result = result.concat(flattenCategories(children));
                }
            });
            return result;
        }
        
        // 渲染分类树
        function renderCategoryTree() {
            const container = document.getElementById('category-tree');
            
            // 确保 categories 是数组
            if (!Array.isArray(categories)) {
                console.error('[前端] renderCategoryTree: categories 不是数组', categories);
                container.innerHTML = '<div class="text-center text-red-500 py-8">数据格式错误</div>';
                return;
            }
            
            if (categories.length === 0) {
                container.innerHTML = '<div class="text-center text-gray-500 py-8">暂无分类，请添加分类</div>';
                return;
            }
            
            container.innerHTML = '';
            categories.forEach(cat => {
                container.appendChild(renderCategoryNode(cat, 0));
            });
        }
        
        // 渲染分类节点
        function renderCategoryNode(category, level) {
            const node = document.createElement('div');
            node.className = 'category-node';
            node.style.marginLeft = (level * 1.5) + 'rem';
            
            const item = document.createElement('div');
            item.className = 'category-item';
            
            const nameSpan = document.createElement('span');
            nameSpan.className = 'category-name';
            // 兼容两种字段名格式
            nameSpan.textContent = category.categoryName || category.CategoryName || '未知';
            
            const idSpan = document.createElement('span');
            idSpan.className = 'category-id';
            // 兼容两种字段名格式
            idSpan.textContent = '(' + (category.categoryId || category.CategoryID || '未知') + ')';
            
            const actions = document.createElement('div');
            actions.className = 'ml-auto flex gap-2';
            
            const addBtn = document.createElement('button');
            addBtn.className = 'btn-action';
            addBtn.textContent = '添加子分类';
            addBtn.onclick = () => openAddModal(category);
            
            const editBtn = document.createElement('button');
            editBtn.className = 'btn-action';
            editBtn.textContent = '编辑';
            editBtn.onclick = () => openEditModal(category);
            
            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'btn-action btn-action-delete';
            deleteBtn.textContent = '删除';
            deleteBtn.onclick = () => openDeleteModal(category);
            
            actions.appendChild(addBtn);
            actions.appendChild(editBtn);
            actions.appendChild(deleteBtn);
            
            item.appendChild(nameSpan);
            item.appendChild(idSpan);
            item.appendChild(actions);
            node.appendChild(item);
            
            // 渲染子分类
            if (category.children && category.children.length > 0) {
                const childrenDiv = document.createElement('div');
                childrenDiv.className = 'category-children';
                category.children.forEach(child => {
                    childrenDiv.appendChild(renderCategoryNode(child, level + 1));
                });
                node.appendChild(childrenDiv);
            }
            
            return node;
        }
        
        // 打开添加分类弹窗
        function openAddModal(parentCategory) {
            editingCategory = null;
            const parentName = parentCategory ? (parentCategory.categoryName || parentCategory.CategoryName || '') : '';
            document.getElementById('modal-title').textContent = parentCategory ? 
                '添加子分类（父分类：' + parentName + '）' : '添加顶级分类';
            document.getElementById('category-form').reset();
            document.getElementById('edit-category-id').value = '';
            document.getElementById('category-id').disabled = false;
            
            const parentInfoBlock = document.getElementById('parent-info-block');
            const parentInfoText = document.getElementById('parent-info-text');
            
            if (parentCategory) {
                const parentId = parentCategory.categoryId || parentCategory.CategoryID || '';
                document.getElementById('parent-category-id').value = parentId;
                parentInfoBlock.classList.remove('hidden');
                parentInfoText.textContent = parentName + ' (' + parentId + ')';
            } else {
                document.getElementById('parent-category-id').value = '';
                parentInfoBlock.classList.add('hidden');
                parentInfoText.textContent = '无（顶级分类）';
            }
            
            document.getElementById('category-modal').classList.remove('hidden');
        }
        
        // 打开编辑分类弹窗
        function openEditModal(category) {
            editingCategory = category;
            document.getElementById('modal-title').textContent = '编辑分类';
            const categoryId = category.categoryId || category.CategoryID || '';
            const categoryName = category.categoryName || category.CategoryName || '';
            const parentCategoryId = category.parentCategoryId || category.ParentCategoryID || '';
            
            document.getElementById('edit-category-id').value = categoryId;
            document.getElementById('category-id').value = categoryId;
            document.getElementById('category-id').disabled = true;
            document.getElementById('category-name').value = categoryName;
            document.getElementById('parent-category-id').value = parentCategoryId;
            
            const parentInfoBlock = document.getElementById('parent-info-block');
            const parentInfoText = document.getElementById('parent-info-text');
            if (parentCategoryId) {
                parentInfoBlock.classList.remove('hidden');
                parentInfoText.textContent = parentCategoryId;
            } else {
                parentInfoBlock.classList.add('hidden');
                parentInfoText.textContent = '无（顶级分类）';
            }
            
            document.getElementById('category-modal').classList.remove('hidden');
        }
        
        // 已去掉父分类下拉选择，保留函数占位以兼容旧调用（不做任何操作）
        function updateParentCategoryOptions() {
            return;
        }
        
        // 关闭弹窗
        function closeModal() {
            document.getElementById('category-modal').classList.add('hidden');
            editingCategory = null;
        }
        
        // 保存分类
        async function saveCategory() {
            const categoryId = document.getElementById('category-id').value.trim();
            const categoryName = document.getElementById('category-name').value.trim();
            const parentCategoryIdEl = document.getElementById('parent-category-id');
            const parentCategoryId = parentCategoryIdEl ? parentCategoryIdEl.value.trim() : '';
            
            if (!categoryId || !categoryName) {
                alert('请填写完整信息');
                return;
            }
            
            try {
                if (editingCategory) {
                    // 更新
                    const response = await axios.put(
                        '<%= request.getContextPath() %>/api/admin/category/' + categoryId,
                        {
                            categoryId: categoryId,
                            categoryName: categoryName,
                            parentCategoryId: parentCategoryId || null
                        }
                    );
                    
                    if (response.data.success) {
                        alert('分类更新成功');
                        closeModal();
                        loadCategoryTree();
                    } else {
                        alert('更新失败：' + (response.data.error || '未知错误'));
                    }
                } else {
                    // 添加
                    const response = await axios.post(
                        '<%= request.getContextPath() %>/api/admin/category',
                        {
                            categoryId: categoryId,
                            categoryName: categoryName,
                            parentCategoryId: parentCategoryId || null
                        }
                    );
                    
                    if (response.data.success) {
                        alert('分类添加成功');
                        closeModal();
                        loadCategoryTree();
                    } else {
                        alert('添加失败：' + (response.data.error || '未知错误'));
                    }
                }
            } catch (error) {
                console.error('保存分类失败:', error);
                const errorMsg = error.response?.data?.error || error.message || '保存失败';
                alert('保存失败：' + errorMsg);
            }
        }
        
        // 打开删除确认弹窗
        function openDeleteModal(category) {
            deletingCategory = category;
            const categoryName = category.categoryName || category.CategoryName || '未知';
            document.getElementById('delete-category-name').textContent = categoryName;
            document.getElementById('delete-modal').classList.remove('hidden');
        }
        
        // 确认删除
        async function confirmDelete() {
            if (!deletingCategory) return;
            
            const categoryId = deletingCategory.categoryId || deletingCategory.CategoryID;
            if (!categoryId) {
                alert('无法获取分类ID');
                return;
            }
            
            try {
                const response = await axios.delete(
                    '<%= request.getContextPath() %>/api/admin/category/' + categoryId
                );
                
                if (response.data.success) {
                    alert('分类删除成功');
                    closeDeleteModal();
                    loadCategoryTree();
                } else {
                    alert('删除失败：' + (response.data.error || '未知错误'));
                }
            } catch (error) {
                console.error('删除分类失败:', error);
                const errorMsg = error.response?.data?.error || error.message || '删除失败';
                alert('删除失败：' + errorMsg);
            }
        }
        
        // 关闭删除确认弹窗
        function closeDeleteModal() {
            document.getElementById('delete-modal').classList.add('hidden');
            deletingCategory = null;
        }
    </script>
</body>
</html>

