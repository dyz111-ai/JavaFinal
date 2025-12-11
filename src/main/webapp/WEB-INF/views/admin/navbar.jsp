<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<style>
    .nav-link {
        @apply px-4 py-2 text-gray-600 hover:text-blue-600 transition-colors duration-200;
    }
    .nav-link.active {
        @apply text-blue-600 font-medium;
    }
</style>

<nav class="bg-white shadow-md">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
            <div class="flex">
                <div class="flex-shrink-0 flex items-center">
                    <a href="<%= request.getContextPath() %>/admin/dashboard" class="text-xl font-bold text-gray-800">
                        图书馆管理系统 - 管理员控制台
                    </a>
                </div>
                <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
                    <a href="<%= request.getContextPath() %>/admin/dashboard" class="nav-link">
                        控制台
                    </a>
                    <a href="<%= request.getContextPath() %>/admin/books" class="nav-link">
                        图书管理
                    </a>
                    <a href="<%= request.getContextPath() %>/admin/announcements" class="nav-link">
                        公告管理
                    </a>
                </div>
            </div>
            <div class="flex items-center">
                <span class="text-gray-500 mr-4">管理员</span>
                <a href="<%= request.getContextPath() %>/auth/logout" class="bg-gray-100 px-4 py-2 rounded-md text-gray-700 hover:bg-gray-200 transition-colors">
                    退出登录
                </a>
            </div>
        </div>
    </div>
</nav>