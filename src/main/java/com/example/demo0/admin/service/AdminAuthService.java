package com.example.demo0.admin.service;

import com.example.demo0.admin.entity.Librarian;
import com.example.demo0.admin.repository.LibrarianRepository;
import org.mindrot.jbcrypt.BCrypt;

public class AdminAuthService {

    private final LibrarianRepository repository = new LibrarianRepository();

    public Librarian login(String staffNo, String rawPassword) {
        System.out.println("=== 管理员登录调试开始 ===");
        System.out.println("尝试登录工号: " + staffNo);
        System.out.println("用户输入密码: [" + rawPassword + "]");

        Librarian admin = repository.findByStaffNo(staffNo);

        if (admin == null) {
            System.out.println("错误: 数据库中未找到工号 " + staffNo);
            throw new RuntimeException("工号不存在");
        }

        String dbPassword = admin.getPassword();
        System.out.println("数据库存储密码: [" + dbPassword + "]");

        boolean isMatch = false;

        // 1. 判断是否为 BCrypt 加密格式（以 $2a$ 开头）
        if (dbPassword != null && dbPassword.startsWith("$2a$")) {
            System.out.println("检测到加密密码，尝试 BCrypt 解密...");
            try {
                isMatch = BCrypt.checkpw(rawPassword, dbPassword);
                System.out.println("BCrypt 验证结果: " + isMatch);
            } catch (Exception e) {
                System.out.println("BCrypt 验证异常，回退到明文比对: " + e.getMessage());
                isMatch = rawPassword.equals(dbPassword);
            }
        } else {
            // 2. 否则直接比较明文
            System.out.println("检测到明文密码，直接比对字符串...");
            // 注意：这里使用了 trim() 防止数据库里有不小心录入的空格
            String cleanDbPass = dbPassword != null ? dbPassword.trim() : "";
            isMatch = rawPassword.equals(cleanDbPass);
            System.out.println("明文比对结果: " + isMatch);
        }

        System.out.println("=== 管理员登录调试结束 ===");

        if (isMatch) {
            return admin;
        } else {
            throw new RuntimeException("密码错误");
        }
    }
}