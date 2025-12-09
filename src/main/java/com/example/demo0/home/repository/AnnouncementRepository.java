package com.example.demo0.home.repository;

import com.example.demo0.home.model.Announcement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 公告数据访问层
 */
public class AnnouncementRepository {
    private static final String JNDI_NAME = "java:/jdbc/LibraryDS";
    private final DataSource dataSource;

    public AnnouncementRepository() {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup(JNDI_NAME);
        } catch (NamingException e) {
            throw new RuntimeException("JNDI 数据源未找到: " + JNDI_NAME, e);
        }
    }

    /**
     * 获取最新的几条公告
     * @param limit 要获取的公告数量
     * @return 公告列表
     */
    public List<Announcement> getLatestAnnouncements(int limit) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "SELECT AnnouncementID, Title, Content, CreateTime FROM public.announcement ORDER BY CreateTime DESC LIMIT ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Announcement announcement = new Announcement();
                    announcement.setAnnouncementId(rs.getInt("AnnouncementID"));
                    announcement.setTitle(rs.getString("Title"));
                    announcement.setContent(rs.getString("Content"));
                    announcement.setCreateTime(rs.getTimestamp("CreateTime"));
                    announcements.add(announcement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询公告失败: " + e.getMessage(), e);
        }
        return announcements;
    }
}




