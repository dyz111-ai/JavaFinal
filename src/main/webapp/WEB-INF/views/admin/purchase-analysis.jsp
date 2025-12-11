<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<% response.setContentType("text/html; charset=UTF-8"); %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>采购分析 - 图书馆管理系统</title>
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
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 10px;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }
        th {
            background-color: #f3f4f6;
            font-weight: 600;
        }
        .btn-primary {
            background-color: #4f46e5;
            color: white;
            padding: 8px 16px;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            border: none;
        }
        .btn-primary:hover {
            background-color: #4338ca;
        }
        .input-field {
            width: 100%;
            padding: 8px;
            border: 1px solid #d1d5db;
            border-radius: 6px;
        }
        .input-field:focus {
            outline: none;
            border-color: #4f46e5;
        }
        .error-message {
            color: #ef4444;
            font-size: 14px;
        }
        .success-message {
            color: #10b981;
            font-size: 14px;
        }
    </style>
</head>
<body class="bg-gray-50 min-h-screen">
<jsp:include page="navbar.jsp" />

<div class="container mx-auto px-4 py-8">
    <div class="dashboard-container">
        <h2 class="text-2xl font-bold mb-6 text-gray-800">
            <i class="fa fa-bar-chart"></i> 采购分析
        </h2>
        
        <!-- 分析概览 - 简化为一行一个卡片 -->
        <div class="space-y-6 mb-8">
            <!-- 借阅次数排行 -->
            <div class="section-card">
                <h3 class="text-lg font-semibold mb-4">借阅次数排行</h3>
                <div id="borrowCountTableContainer">
                    <div class="text-center py-8 text-gray-500">加载中...</div>
                </div>
            </div>
            
            <!-- 借阅时长排行 -->
            <div class="section-card">
                <h3 class="text-lg font-semibold mb-4">借阅时长排行</h3>
                <div id="borrowDurationTableContainer">
                    <div class="text-center py-8 text-gray-500">加载中...</div>
                </div>
            </div>
            
            <!-- 实例借阅排行 -->
            <div class="section-card">
                <h3 class="text-lg font-semibold mb-4">实例借阅排行</h3>
                <div id="instanceBorrowTableContainer">
                    <div class="text-center py-8 text-gray-500">加载中...</div>
                </div>
            </div>
        </div>
        
        <!-- 采购日志管理 -->
        <div class="section-card">
            <h3 class="text-lg font-semibold mb-4">采购日志管理</h3>
            
            <!-- 添加日志表单 -->
            <div class="mb-6 bg-gray-50 p-4 rounded-lg">
                <h4 class="font-medium mb-3">添加新采购日志</h4>
                <div class="flex gap-4">
                    <input type="text" id="logText" class="input-field flex-1" placeholder="输入采购日志内容..." />
                    <button id="addLogBtn" class="btn-primary whitespace-nowrap">
                        <i class="fa fa-plus"></i> 添加日志
                    </button>
                </div>
                <div id="logMessage" class="mt-2 text-sm"></div>
            </div>
            
            <!-- 日志列表 -->
            <div>
                <table id="logsTable">
                    <thead>
                        <tr>
                            <th>日志ID</th>
                            <th>日志内容</th>
                            <th>记录时间</th>
                            <th>管理员ID</th>
                        </tr>
                    </thead>
                    <tbody id="logsTableBody">
                        <tr>
                            <td colspan="4" class="text-center py-8 text-gray-500">加载日志中...</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    // 设置页面编码
    document.charset = 'UTF-8';
    
    // 页面加载时获取分析数据
    window.onload = function() {
        fetchAnalysisData();
        fetchLogsData();
        
        // 添加日志按钮事件
        document.getElementById('addLogBtn').onclick = function() {
            addPurchaseLog();
        };
        
        // 回车键添加日志
        document.getElementById('logText').onkeypress = function(e) {
            if (e.key === 'Enter') {
                addPurchaseLog();
            }
        };
    };
    
    // 获取分析数据
    function fetchAnalysisData() {
        var contextPath = '<%= request.getContextPath() %>';
        fetch(contextPath + '/api/admin/purchase-analysis')
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('请求失败');
                }
                return response.json();
            })
            .then(function(data) {
                // 渲染借阅次数排行
                renderBookTable('borrowCountTableContainer', data.topByBorrowCount, '借阅次数');
                
                // 渲染借阅时长排行
                renderBookTable('borrowDurationTableContainer', data.topByBorrowDuration, '借阅时长(天)');
                
                // 渲染实例借阅排行
                renderBookTable('instanceBorrowTableContainer', data.topByInstanceBorrow, '实例借阅数');
            })
            .catch(function(error) {
                console.error('获取分析数据失败:', error);
                document.getElementById('borrowCountTableContainer').innerHTML = '<div class="text-center py-8 text-red-500">加载数据失败</div>';
                document.getElementById('borrowDurationTableContainer').innerHTML = '<div class="text-center py-8 text-red-500">加载数据失败</div>';
                document.getElementById('instanceBorrowTableContainer').innerHTML = '<div class="text-center py-8 text-red-500">加载数据失败</div>';
            });
    }
    
    // 渲染图书排行表格
    function renderBookTable(containerId, books, metricLabel) {
        var container = document.getElementById(containerId);
        
        if (!books || books.length === 0) {
            container.innerHTML = '<div class="text-center py-8 text-gray-500">暂无数据</div>';
            return;
        }
        
        var tableHtml = '<table><thead><tr><th>排名</th><th>ISBN</th><th>书名</th><th>作者</th><th>' + metricLabel + '</th></tr></thead><tbody>';
        
        for (var i = 0; i < books.length; i++) {
            var book = books[i];
            tableHtml += '<tr>';
            tableHtml += '<td>' + (i + 1) + '</td>';
            tableHtml += '<td>' + (book.isbn || '-') + '</td>';
            tableHtml += '<td>' + (book.title || '-') + '</td>';
            tableHtml += '<td>' + (book.author || '-') + '</td>';
            tableHtml += '<td>' + (book.metricValue || 0) + '</td>';
            tableHtml += '</tr>';
        }
        
        tableHtml += '</tbody></table>';
        container.innerHTML = tableHtml;
    }
    
    // 获取日志数据
    function fetchLogsData() {
        var contextPath = '<%= request.getContextPath() %>';
        fetch(contextPath + '/api/admin/purchase-analysis/logs')
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('请求失败');
                }
                return response.json();
            })
            .then(function(logs) {
                renderLogsTable(logs);
            })
            .catch(function(error) {
                console.error('获取日志数据失败:', error);
                document.getElementById('logsTableBody').innerHTML = '<tr><td colspan="4" class="text-center text-red-500 py-8">加载日志失败</td></tr>';
            });
    }
    
    // 渲染日志表格
    function renderLogsTable(logs) {
        var tbody = document.getElementById('logsTableBody');
        
        if (!logs || logs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center py-8 text-gray-500">暂无日志记录</td></tr>';
            return;
        }
        
        tbody.innerHTML = '';
        
        for (var i = 0; i < logs.length; i++) {
            var log = logs[i];
            var tr = document.createElement('tr');
            var formattedDate = log.logDate ? new Date(log.logDate).toLocaleString('zh-CN') : '-';
            
            tr.innerHTML = '<td>' + (log.logId || '-') + '</td>' +
                          '<td>' + (log.logText || '-') + '</td>' +
                          '<td>' + formattedDate + '</td>' +
                          '<td>' + (log.adminId || '-') + '</td>';
            
            tbody.appendChild(tr);
        }
    }
    
    // 添加采购日志
    function addPurchaseLog() {
        var logText = document.getElementById('logText').value.trim();
        var logMessage = document.getElementById('logMessage');
        
        if (!logText) {
            logMessage.className = 'mt-2 text-sm error-message';
            logMessage.textContent = '请输入日志内容';
            return;
        }
        
        // 隐藏之前的消息
        logMessage.textContent = '';
        
        var addLogBtn = document.getElementById('addLogBtn');
        var originalText = addLogBtn.innerHTML;
        addLogBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i> 添加中...';
        addLogBtn.disabled = true;
        
        var contextPath = '<%= request.getContextPath() %>';
        
        // 使用传统的XMLHttpRequest避免fetch可能的兼容性问题
        var xhr = new XMLHttpRequest();
        xhr.open('POST', contextPath + '/api/admin/purchase-analysis/logs', true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        
        xhr.onload = function() {
            if (xhr.status === 201) {
                // 添加成功
                logMessage.className = 'mt-2 text-sm success-message';
                logMessage.textContent = '日志添加成功';
                document.getElementById('logText').value = '';
                // 重新加载日志列表
                fetchLogsData();
                
                // 3秒后隐藏成功消息
                setTimeout(function() {
                    logMessage.textContent = '';
                }, 3000);
            } else if (xhr.status === 400) {
                try {
                    var data = JSON.parse(xhr.responseText);
                    logMessage.className = 'mt-2 text-sm error-message';
                    logMessage.textContent = data.message || '参数错误';
                } catch (e) {
                    logMessage.className = 'mt-2 text-sm error-message';
                    logMessage.textContent = '请求错误';
                }
            } else {
                logMessage.className = 'mt-2 text-sm error-message';
                logMessage.textContent = '添加日志失败';
            }
            
            // 恢复按钮状态
            addLogBtn.innerHTML = originalText;
            addLogBtn.disabled = false;
        };
        
        xhr.onerror = function() {
            logMessage.className = 'mt-2 text-sm error-message';
            logMessage.textContent = '网络错误';
            // 恢复按钮状态
            addLogBtn.innerHTML = originalText;
            addLogBtn.disabled = false;
        };
        
        // 发送请求
        var requestData = JSON.stringify({
            logText: logText,
            adminId: 1 // 假设当前管理员ID为1
        });
        xhr.send(requestData);
    }
</script>
</body>
</html>