<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
    import="java.util.List"
    import="com.example.demo0.admin.dto.AnnouncementDto"
    import="java.time.LocalDateTime"
    import="java.time.format.DateTimeFormatter"
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>公告管理 - 管理员界面</title>
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
    
    .btn-action-edit {
        color: #2563eb;
        transition: text-decoration 0.2s;
    }
    
    .btn-action-edit:hover {
        text-decoration: underline;
    }
    
    .btn-action-edit:disabled {
        color: #9ca3af;
        text-decoration: none;
    }
    
    .btn-action-delete {
        color: #dc2626;
        transition: text-decoration 0.2s;
    }
    
    .btn-action-delete:hover {
        text-decoration: underline;
    }
    
    .btn-action-delete:disabled {
        color: #9ca3af;
        text-decoration: none;
    }
    
    
    
    .bg-white-opacity-80 {
        background-color: rgba(255, 255, 255, 0.8);
    }
    
    .backdrop-blur-md {
        backdrop-filter: blur(12px);
    }
</style>
</head>
<body class="bg-gray-50 min-h-screen">
    <!-- 导入管理员导航栏 -->
    <jsp:include page="navbar.jsp" />
    
    <div class="container mx-auto px-4 py-8">
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 h-full">
            <!-- 左侧/上半部分：编辑区 -->
            <div class="lg:col-span-1 bg-white-opacity-80 backdrop-blur-md p-6 rounded-lg shadow-md">
                <h2 class="text-xl font-bold mb-4"><span id="form-title">发布新公告</span></h2>
                <form id="announcement-form" onsubmit="handleSubmit(); return false;">
                    <input type="hidden" id="announcement-id" value="" />
                    <div class="space-y-4">
                        <div>
                            <label class="block font-medium">标题</label>
                            <input type="text" id="announcement-title" class="form-input" required>
                        </div>
                        <div>
                            <label class="block font-medium">内容</label>
                            <textarea rows="8" id="announcement-content" class="form-input"></textarea>
                        </div>

                        <div class="flex gap-4">
                            <button type="submit" class="btn-primary w-full" id="submit-btn">发布</button>
                            <button type="button" id="cancel-btn" class="btn-secondary w-full" style="display: none;" onclick="resetForm();">取消修改</button>
                        </div>
                    </div>
                </form>
            </div>

            <!-- 右侧/下半部分：历史公告 -->
            <div class="lg:col-span-2 bg-white-opacity-80 backdrop-blur-md p-6 rounded-lg shadow-md">
               <h2 class="text-xl font-bold mb-4">历史公告</h2>
               <div class="h-[60vh] overflow-y-auto">
                <table class="w-full text-sm text-left">
                    <thead class="text-xs text-gray-700 uppercase bg-gray-50/80">
                        <tr>
                            <th scope="col" class="px-4 py-3">标题</th>

                            <th scope="col" class="px-4 py-3">状态</th>
                            <th scope="col" class="px-4 py-3">发布时间</th>
                            <th scope="col" class="px-4 py-3">操作</th>
                        </tr>
                    </thead>
                    <tbody id="announcements-table-body">
                        <% 
                            // 在实际应用中，这里会从request中获取数据
                            // List<AnnouncementDto> announcements = (List<AnnouncementDto>) request.getAttribute("announcements");
                            // if (announcements != null) {
                            //     for (AnnouncementDto ann : announcements) {
                            //         // 渲染表格行
                            //     }
                            // }
                        %>
                        <!-- 表格内容将通过JavaScript动态填充 -->
                    </tbody>
                </table>
               </div>
            </div>
        </div>
    </div>
    
    <script>
        let editingId = null;
        
        // 页面加载时获取所有公告
        document.addEventListener('DOMContentLoaded', function() {
            fetchData();
        });
        
        // 获取所有公告
        async function fetchData() {
            try {
                const response = await axios.get('<%= request.getContextPath() %>/api/admin/announcements');
                const announcements = response.data;
                renderAnnouncements(announcements);
            } catch (error) {
                console.error("获取公告失败:", error);
                alert("获取公告数据失败，请刷新页面重试");
            }
        }
        
        // 渲染公告列表
        function renderAnnouncements(announcements) {
            const tableBody = document.getElementById('announcements-table-body');
            tableBody.innerHTML = '';
            
            announcements.forEach(ann => {
                const row = document.createElement('tr');
                row.className = 'border-b';
                
                // 格式化日期
                const createTime = new Date(ann.createTime).toLocaleString();
                

                // 构建禁用属性
                var disabledAttr = ann.status === '已撤回' ? 'disabled' : '';
                
                row.innerHTML = 
                    '<td class="px-4 py-3 font-medium">' + ann.title + '</td>' + 

                    '<td class="px-4 py-3">' + ann.status + '</td>' + 
                    '<td class="px-4 py-3">' + createTime + '</td>' + 
                    '<td class="px-4 py-3 flex gap-2">' + 
                        '<button onclick="editAnnouncement(' + ann.announcementId + ')" ' + disabledAttr + ' class="btn-action-edit">修改</button>' + 
                        '<button onclick="takedown(' + ann.announcementId + ')" ' + disabledAttr + ' class="btn-action-delete">下架</button>' + 
                    '</td>';
                
                tableBody.appendChild(row);
            });
        }
        
        // 编辑公告
        async function editAnnouncement(id) {
            try {
                // 由于后端API设计，我们可能需要从已获取的数据中查找，而不是单独请求
                // 这里我们保持原逻辑，如果后端支持单个公告查询则正常工作，否则会提示错误
                const response = await axios.get('<%= request.getContextPath() %>/api/admin/announcements/' + id);
                const announcement = response.data;
                
                editingId = id;
                document.getElementById('announcement-id').value = announcement.announcementId;
                document.getElementById('announcement-title').value = announcement.title;
                document.getElementById('announcement-content').value = announcement.content;
                document.getElementById('form-title').textContent = '修改公告';
                document.getElementById('submit-btn').textContent = '更新';
                document.getElementById('cancel-btn').style.display = 'block';
                
                // 滚动到表单区域
                document.getElementById('announcement-form').scrollIntoView({ behavior: 'smooth' });
            } catch (error) {
                console.error('获取公告详情失败:', error);
                alert('获取公告详情失败，请重试');
            }
        }
        
        // 重置表单
        function resetForm() {
            document.getElementById('announcement-form').reset();
            document.getElementById('announcement-id').value = '';
            document.getElementById('form-title').textContent = '发布新公告';
            document.getElementById('submit-btn').textContent = '发布';
            document.getElementById('cancel-btn').style.display = 'none';
            editingId = null;
        }
        
        // 提交表单
        async function handleSubmit() {
            const announcementId = document.getElementById('announcement-id').value;
            const title = document.getElementById('announcement-title').value;
            const content = document.getElementById('announcement-content').value;
            
            if (!title || !content) {
                alert('请填写标题和内容');
                return;
            }
            
            try {
                const data = {
                    title: title,
                    content: content
                    // 后端会自动设置发布时间和默认状态
                };
                
                let url = '<%= request.getContextPath() %>/api/admin/announcements';
                let method = 'post';
                
                if (announcementId) {
                    url = '<%= request.getContextPath() %>/api/admin/announcements/' + announcementId;
                    method = 'put';
                    await axios.put(url, data);
                    alert('公告更新成功');
                } else {
                    // 在演示环境中使用默认管理员ID 1
                    // 在实际应用中，这应该从当前登录用户的信息中获取
                    const adminId = 1; // 模拟从认证上下文中获取的管理员ID
                    await axios.post(url + '?adminId=' + adminId, data);
                    alert('公告发布成功');
                }
                
                resetForm();
                fetchData(); // 刷新列表
            } catch (error) {
                console.error('操作失败:', error);
                alert('操作失败，请重试');
            }
        }
        
        // 下架公告
        async function takedown(id) {
            if (!confirm('确定要下架这个公告吗？')) {
                return;
            }
            
            try {
                // 直接调用后端的下架API
                await axios.put('<%= request.getContextPath() %>/api/admin/announcements/' + id + '/takedown');
                alert('公告已下架');
                fetchData(); // 刷新列表
            } catch (error) {
                console.error('下架失败:', error);
                alert('下架失败，请重试');
            }
        }
    </script>
</body>
</html>
