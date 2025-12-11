<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<style>
    .nav-link {
        padding: 8px 16px;
        color: #4B5563;
        text-decoration: none;
    }
    .nav-link:hover {
        color: #2563EB;
    }
    .nav-link.active {
        color: #2563EB;
        font-weight: 500;
    }
    .logout-btn {
        background-color: #EF4444;
        color: white;
        padding: 8px 16px;
        border-radius: 6px;
        text-decoration: none;
        transition: background-color 0.2s;
    }
    .logout-btn:hover {
        background-color: #DC2626;
        color: white;
    }
</style>

<nav style="background-color: white; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
    <div style="max-width: 80rem; margin: 0 auto; padding: 0 16px;">
        <div style="display: flex; justify-content: space-between; height: 4rem; align-items: center;">
            <div>
                <span style="background-color: #3B82F6; color: white; font-weight: bold; padding: 8px 16px; border-radius: 6px;">管理员</span>
            </div>
            <div>
                <a href="<%= request.getContextPath() %>/auth/logout" class="logout-btn">
                    退出登录
                </a>
            </div>
        </div>
    </div>
</nav>