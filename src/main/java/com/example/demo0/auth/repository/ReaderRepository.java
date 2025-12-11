package com.example.demo0.auth.repository;

import com.example.demo0.auth.model.Reader;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReaderRepository {
    private final DataSource dataSource;

    public ReaderRepository() {
        try {
            InitialContext ctx = new InitialContext();
            // 确保这里的 JNDI 名称和你 standalone.xml 配置的一致
            this.dataSource = (DataSource) ctx.lookup("java:/jdbc/LibraryDS");
        } catch (NamingException e) {
            throw new RuntimeException("数据源查找失败: " + e.getMessage());
        }
    }

    // 根据用户名查找用户
    public Reader findByUsername(String username) {
        String sql = "SELECT ReaderID, Username, Password, Fullname, Nickname, Avatar, CreditScore, AccountStatus, Permission FROM Reader WHERE Username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reader reader = new Reader();
                    reader.setReaderId(rs.getInt("ReaderID"));
                    reader.setUsername(rs.getString("Username"));
                    reader.setPassword(rs.getString("Password"));
                    reader.setFullname(rs.getString("Fullname"));
                    reader.setNickname(rs.getString("Nickname"));
                    reader.setAvatar(rs.getString("Avatar"));
                    reader.setCreditScore(rs.getInt("CreditScore"));
                    reader.setAccountStatus(rs.getString("AccountStatus"));
                    reader.setPermission(rs.getString("Permission"));
                    return reader;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 创建新用户
    public void create(Reader reader) {
        String sql = "INSERT INTO Reader (Username, Password, Fullname, Nickname) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reader.getUsername());
            ps.setString(2, reader.getPassword());
            ps.setString(3, reader.getFullname());
            ps.setString(4, reader.getNickname());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("注册失败: " + e.getMessage());
        }
    }

    // 判断除自己外是否存在相同用户名
    public boolean existsUsernameExcept(String username, int readerId) {
        String sql = "SELECT COUNT(1) FROM Reader WHERE Username = ? AND ReaderID <> ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("检查用户名唯一性失败: " + e.getMessage());
        }
        return false;
    }

    // 根据ID查找用户
    public Reader findById(Integer readerId) {
        if (readerId == null) return null;
        
        String sql = "SELECT ReaderID, Username, Password, Fullname, Nickname, Avatar, CreditScore, AccountStatus, Permission FROM Reader WHERE ReaderID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reader reader = new Reader();
                    reader.setReaderId(rs.getInt("ReaderID"));
                    reader.setUsername(rs.getString("Username"));
                    reader.setPassword(rs.getString("Password"));
                    reader.setFullname(rs.getString("Fullname"));
                    reader.setNickname(rs.getString("Nickname"));
                    reader.setAvatar(rs.getString("Avatar"));
                    reader.setCreditScore(rs.getInt("CreditScore"));
                    reader.setAccountStatus(rs.getString("AccountStatus"));
                    reader.setPermission(rs.getString("Permission"));
                    return reader;
                }
            }
        } catch (SQLException e) {
            System.err.println("查询读者信息失败: " + e.getMessage());
        }
        return null;
    }

    // 更新用户资料：用户名、真实姓名、昵称、头像
    public void update(Reader reader) {
        String sql = "UPDATE Reader SET Username = ?, Fullname = ?, Nickname = ?, Avatar = ? WHERE ReaderID = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reader.getUsername());
            ps.setString(2, reader.getFullname());
            ps.setString(3, reader.getNickname());
            ps.setString(4, reader.getAvatar());
            ps.setInt(5, reader.getReaderId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新资料失败: " + e.getMessage());
        }
    }
}