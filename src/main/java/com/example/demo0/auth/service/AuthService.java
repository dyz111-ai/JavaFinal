package com.example.demo0.auth.service;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.auth.repository.ReaderRepository; // 必须导入这个
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {

    // 【关键点】你之前可能漏掉了这行变量声明！
    // 只有声明了这个变量，下面的方法才能使用 'repository'
    private final ReaderRepository repository = new ReaderRepository();

    // 注册业务
    public void register(String username, String rawPassword, String fullname, String nickname) {
        if (repository.findByUsername(username) != null) {
            throw new RuntimeException("该用户名已被注册");
        }
        // BCrypt 加密
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        Reader reader = new Reader();
        reader.setUsername(username);
        reader.setPassword(hashedPassword);
        reader.setFullname(fullname);
        reader.setNickname((nickname == null || nickname.trim().isEmpty()) ? username : nickname);

        repository.create(reader);
    }

    // 登录业务
    public Reader login(String username, String rawPassword) {
        Reader reader = repository.findByUsername(username);
        if (reader != null && BCrypt.checkpw(rawPassword, reader.getPassword())) {
            if ("冻结".equals(reader.getAccountStatus())) {
                throw new RuntimeException("账号已被冻结");
            }
            return reader;
        }
        return null;
    }

    // 【新增】更新个人信息
    public void updateProfile(Reader reader) {
        if (reader.getNickname() == null || reader.getNickname().trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }
        // 这里调用上面声明的 repository 变量
        repository.update(reader);
    }
}