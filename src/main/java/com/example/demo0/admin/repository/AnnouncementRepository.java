package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Announcement;
import com.example.demo0.admin.dto.UpsertAnnouncementDto;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementRepository {

    private final DataSource dataSource;

    public AnnouncementRepository() {
        try {
            // 获取数据源
            this.dataSource = (DataSource) new InitialContext().lookup("java:/jdbc/LibraryDS");
        } catch (Exception e) {
            throw new RuntimeException("JNDI 数据源查找失败", e);
        }
    }

    // 获取所有公告
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM public.announcement ORDER BY createtime DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 创建公告
    public Announcement createAnnouncement(UpsertAnnouncementDto dto, Integer librarianId) {
        String sql = "INSERT INTO public.announcement (librarianid, title, content, createtime, targetgroup, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, librarianId);
            ps.setString(2, dto.getTitle());
            ps.setString(3, dto.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(5, dto.getTargetGroup() != null ? dto.getTargetGroup() : "全员");
            ps.setString(6, "发布中");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return findById(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("创建公告失败", e);
        }
        return null;
    }

    // 更新公告
    public Announcement updateAnnouncement(Integer id, UpsertAnnouncementDto dto) {
        String sql = "UPDATE public.announcement SET title=?, content=?, targetgroup=? WHERE announcementid=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dto.getTitle());
            ps.setString(2, dto.getContent());
            ps.setString(3, dto.getTargetGroup());
            ps.setInt(4, id);

            ps.executeUpdate();
            return findById(id);
        } catch (SQLException e) {
            throw new RuntimeException("更新公告失败", e);
        }
    }

    // 更新状态（如下架）
    public boolean updateStatus(Integer id, String status) {
        String sql = "UPDATE public.announcement SET status=? WHERE announcementid=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据ID查找
    public Announcement findById(Integer id) {
        String sql = "SELECT * FROM public.announcement WHERE announcementid=?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 辅助映射方法
    private Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setAnnouncementId(rs.getInt("announcementid"));
        a.setLibrarianId(rs.getInt("librarianid"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setTargetGroup(rs.getString("targetgroup"));
        a.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("createtime");
        if (ts != null) a.setCreateTime(ts.toLocalDateTime());
        return a;
    }
}