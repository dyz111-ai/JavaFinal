package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Librarian;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LibrarianRepository {

    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";
    private final DataSource dataSource;

    public LibrarianRepository() {
        try {
            InitialContext ctx = new InitialContext();
            this.dataSource = (DataSource) ctx.lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException("数据源查找失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据工号查找管理员
     */
    public Librarian findByStaffNo(String staffNo) {
        String sql = "SELECT * FROM public.librarian WHERE staffno = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staffNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Librarian lib = new Librarian();
                    lib.setLibrarianId(rs.getInt("librarianid"));
                    lib.setStaffNo(rs.getString("staffno"));
                    lib.setPassword(rs.getString("password"));
                    lib.setName(rs.getString("name"));
                    lib.setPermission(rs.getString("permission"));
                    return lib;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询管理员失败", e);
        }
        return null;
    }
}