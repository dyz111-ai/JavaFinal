<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>举报处理 - 图书馆管理系统</title>
    <link href="https://cdn.tailwindcss.com" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f9fafb;
        }
        .section-card {
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            border: 1px solid #e5e7eb;
            padding: 20px;
            margin-bottom: 20px;
        }
        .report-card {
            background-color: white;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 16px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
        }
        .report-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;
            padding-bottom: 12px;
            border-bottom: 1px solid #e5e7eb;
        }
        .report-content {
            margin-bottom: 12px;
        }
        .report-actions {
            display: flex;
            gap: 8px;
            justify-content: flex-end;
        }
        .btn-primary {
            background-color: #4f46e5;
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            border: none;
        }
        .btn-primary:hover {
            background-color: #4338ca;
        }
        .btn-success {
            background-color: #10b981;
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            border: none;
        }
        .btn-success:hover {
            background-color: #059669;
        }
        .btn-danger {
            background-color: #ef4444;
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            border: none;
        }
        .btn-danger:hover {
            background-color: #dc2626;
        }
        .btn-secondary {
            background-color: #6b7280;
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            border: none;
        }
        .btn-secondary:hover {
            background-color: #4b5563;
        }
        .status-pending {
            color: #f59e0b;
            font-weight: 500;
        }
        .status-approved {
            color: #10b981;
            font-weight: 500;
        }
        .status-rejected {
            color: #ef4444;
            font-weight: 500;
        }
        .error-message {
            color: #ef4444;
            font-size: 14px;
        }
        .success-message {
            color: #10b981;
            font-size: 14px;
        }
        .comment-preview {
            background-color: #f3f4f6;
            padding: 12px;
            border-radius: 6px;
            margin: 8px 0;
            font-style: italic;
        }
    </style>
</head>
<body class="bg-gray-50 min-h-screen">
<jsp:include page="navbar.jsp" />

<div class="container mx-auto px-4 py-8">
    <div class="dashboard-container">
        <h2 class="text-2xl font-bold mb-6 text-gray-800">
            <i class="fa fa-flag"></i> 举报处理
        </h2>
        
        <div id="messageContainer" class="mb-4 p-3 rounded-lg hidden"></div>
        
        <!-- 举报列表 -->
        <div class="section-card">
            <div class="flex justify-between items-center mb-4">
                <h3 class="text-lg font-semibold">待处理举报</h3>
                <button id="refreshBtn" class="btn-secondary">
                    <i class="fa fa-refresh"></i> 刷新列表
                </button>
            </div>
            
            <div id="reportsContainer" class="space-y-4">
                <div class="text-center py-10 text-gray-500">加载中...</div>
            </div>
        </div>
    </div>
</div>

<script>
    // 设置页面编码
    document.charset = 'UTF-8';
    
    // 从session获取当前管理员ID，如果不存在则使用默认值
    var currentLibrarianId = <%= session.getAttribute("currentAdminId") != null ? session.getAttribute("currentAdminId") : 1 %>;
    
    // 页面加载时获取待处理举报
    window.onload = function() {
        fetchPendingReports();
        
        // 刷新按钮事件
        document.getElementById('refreshBtn').onclick = function() {
            fetchPendingReports();
        };
    };
    
    // 获取待处理举报列表
    function fetchPendingReports() {
        var contextPath = '<%= request.getContextPath() %>';
        var reportsContainer = document.getElementById('reportsContainer');
        
        // 显示加载状态
        reportsContainer.innerHTML = '<div class="text-center py-10 text-gray-500">正在获取举报列表...</div>';
        
        var xhr = new XMLHttpRequest();
        xhr.open('GET', contextPath + '/api/admin/reports/pending?librarianId=' + currentLibrarianId, true);
        // 设置响应类型
        xhr.responseType = 'json';
        
        xhr.onload = function() {
            if (xhr.status === 200) {
                try {
                    // XMLHttpRequest已经解析了JSON，直接使用response
                    var reports = xhr.response || [];
                    renderReportsList(reports);
                } catch (e) {
                    console.error('解析数据失败:', e);
                    showMessage('错误：解析数据失败', 'error');
                    reportsContainer.innerHTML = '<div class="text-center py-10 text-red-500">加载失败</div>';
                }
            } else if (xhr.status === 401) {
                // 未授权，重定向到登录页
                window.location.href = contextPath + '/auth/login';
            } else {
                showMessage('错误：获取举报列表失败（状态码：' + xhr.status + '）', 'error');
                reportsContainer.innerHTML = '<div class="text-center py-10 text-red-500">加载失败</div>';
            }
        };
        
        xhr.onerror = function() {
            showMessage('网络错误，请检查网络连接', 'error');
            reportsContainer.innerHTML = '<div class="text-center py-10 text-red-500">网络错误</div>';
        };
        
        xhr.send();
    }
    
    // 渲染举报列表
    function renderReportsList(reports) {
        var reportsContainer = document.getElementById('reportsContainer');
        
        if (!reports || reports.length === 0) {
            reportsContainer.innerHTML = '<div class="text-center py-10 text-gray-500">暂无待处理举报</div>';
            return;
        }
        
        reportsContainer.innerHTML = '';
        
        for (var i = 0; i < reports.length; i++) {
            var report = reports[i];
            var reportCard = createReportCard(report);
            reportsContainer.appendChild(reportCard);
        }
    }
    
    // 创建举报卡片
    function createReportCard(report) {
        var div = document.createElement('div');
        div.className = 'report-card';
        div.dataset.reportId = report.reportId;
        
        // 格式化日期
        var reportTimeStr = report.reportTime ? new Date(report.reportTime).toLocaleString('zh-CN') : '-';
        var commentTimeStr = report.commentTime ? new Date(report.commentTime).toLocaleString('zh-CN') : '-';
        
        div.innerHTML = `
            <div class="report-header">
                <div>
                    <div class="flex items-center gap-2 mb-1">
                        <span class="font-medium">举报ID: ${report.reportId}</span>
                        <span class="status-pending">待处理</span>
                    </div>
                    <div class="text-sm text-gray-500">
                        举报时间: ${reportTimeStr}
                    </div>
                </div>
                <div>
                    <span class="text-sm font-medium">图书: ${report.bookTitle || '-'}</span>
                </div>
            </div>
            
            <div class="report-content">
                <div class="mb-3">
                    <div class="font-medium text-sm mb-1">举报原因:</div>
                    <div>${report.reportReason || '-'}</div>
                </div>
                
                <div class="mb-3">
                    <div class="font-medium text-sm mb-1">被举报评论:</div>
                    <div class="comment-preview">${report.reviewContent || '-'}</div>
                    <div class="text-xs text-gray-500 mt-1">
                        评论用户: ${report.commenterNickname || '-'} (ID: ${report.commenterId})
                    </div>
                    <div class="text-xs text-gray-500">
                        评论时间: ${commentTimeStr}
                    </div>
                </div>
                
                <div class="text-sm">
                    <div>举报人: ${report.reporterNickname || '-'} (ID: ${report.reporterId})</div>
                </div>
            </div>
            
            <div class="report-actions">
                <button class="btn-success approve-btn">
                    <i class="fa fa-check"></i> 批准举报
                </button>
                <button class="btn-danger reject-btn">
                    <i class="fa fa-times"></i> 拒绝举报
                </button>
            </div>
        `;
        
        // 添加事件监听
        div.querySelector('.approve-btn').onclick = function() {
            handleReport(report.reportId, 'approve');
        };
        
        div.querySelector('.reject-btn').onclick = function() {
            handleReport(report.reportId, 'reject');
        };
        
        return div;
    }
    
    // 处理举报
    function handleReport(reportId, action) {
        // 确认操作
        var confirmMsg = action === 'approve' ? '确定要批准这个举报吗？' : '确定要拒绝这个举报吗？';
        if (!confirm(confirmMsg)) {
            return;
        }
        
        var contextPath = '<%= request.getContextPath() %>';
        var reportCard = document.querySelector('.report-card[data-report-id="' + reportId + '"]');
        var buttons = reportCard.querySelectorAll('button');
        
        // 禁用按钮并显示加载状态
        for (var i = 0; i < buttons.length; i++) {
            buttons[i].disabled = true;
            buttons[i].innerHTML = '<i class="fa fa-spinner fa-spin"></i> 处理中...';
        }
        
        // 准备请求数据
        var dto = {
            reportId: reportId,
            action: action,
            librarianId: currentLibrarianId
        };
        
        var xhr = new XMLHttpRequest();
        xhr.open('PUT', contextPath + '/api/admin/reports/' + reportId + '?librarianId=' + currentLibrarianId, true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        
        xhr.onload = function() {
            if (xhr.status === 204) {
                showMessage('举报处理成功', 'success');
                
                // 添加淡出动画效果
                reportCard.style.transition = 'opacity 0.3s ease-out';
                reportCard.style.opacity = '0';
                
                // 动画完成后移除元素
                setTimeout(function() {
                    reportCard.remove();
                    
                    // 检查是否还有举报
                    var remainingReports = document.querySelectorAll('.report-card');
                    if (remainingReports.length === 0) {
                        document.getElementById('reportsContainer').innerHTML = 
                            '<div class="text-center py-10 text-gray-500">暂无待处理举报</div>';
                    }
                }, 300);
            } else if (xhr.status === 401) {
                // 未授权，重定向到登录页
                window.location.href = contextPath + '/auth/login';
            } else {
                try {
                    var errorData = JSON.parse(xhr.responseText);
                    showMessage('处理失败: ' + (errorData.message || '未知错误'), 'error');
                } catch (e) {
                    showMessage('处理失败: 服务器错误（状态码：' + xhr.status + '）', 'error');
                }
                
                // 恢复按钮
                restoreButtons(reportCard, action);
            }
        };
        
        xhr.onerror = function() {
            showMessage('网络错误，请检查网络连接', 'error');
            restoreButtons(reportCard, action);
        };
        
        xhr.send(JSON.stringify(dto));
    }
    
    // 恢复按钮状态
    function restoreButtons(reportCard, action) {
        var approveBtn = reportCard.querySelector('.approve-btn');
        var rejectBtn = reportCard.querySelector('.reject-btn');
        
        approveBtn.disabled = false;
        approveBtn.innerHTML = '<i class="fa fa-check"></i> 批准举报';
        
        rejectBtn.disabled = false;
        rejectBtn.innerHTML = '<i class="fa fa-times"></i> 拒绝举报';
    }
    
    // 显示消息
    function showMessage(message, type) {
        var container = document.getElementById('messageContainer');
        container.className = 'mb-4 p-3 rounded-lg ' + 
            (type === 'error' ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700');
        container.textContent = message;
        container.classList.remove('hidden');
        
        // 3秒后隐藏消息
        setTimeout(function() {
            container.classList.add('hidden');
        }, 3000);
    }
</script>
</body>
</html>