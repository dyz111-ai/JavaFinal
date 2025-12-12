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
        System.out.println("[AnnouncementRepository] ========== 开始创建公告 ==========");
        System.out.println("[AnnouncementRepository] LibrarianID: " + librarianId);
        System.out.println("[AnnouncementRepository] Title: " + dto.getTitle());
        System.out.println("[AnnouncementRepository] Content: " + (dto.getContent() != null ? dto.getContent().substring(0, Math.min(50, dto.getContent().length())) + "..." : "null"));
        System.out.println("[AnnouncementRepository] TargetGroup: " + dto.getTargetGroup());
        
        // PostgreSQL 使用 RETURNING 子句获取生成的ID
        String sql = "INSERT INTO public.announcement (librarianid, title, content, createtime, targetgroup, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING announcementid";
        System.out.println("[AnnouncementRepository] SQL: " + sql);
        
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[AnnouncementRepository] ✅ 获取数据库连接成功");
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                LocalDateTime now = LocalDateTime.now();
                // 数据库约束要求：'所有人', '读者', '管理员'
                String targetGroup = dto.getTargetGroup() != null ? dto.getTargetGroup() : "所有人";
                
            ps.setInt(1, librarianId);
            ps.setString(2, dto.getTitle());
            ps.setString(3, dto.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(now));
            // 数据库约束要求：'所有人', '读者', '管理员'
            // 如果传入的值不符合，使用 '所有人' 作为默认值
            String validTargetGroup = targetGroup;
            if (targetGroup == null || 
                (!targetGroup.equals("所有人") && !targetGroup.equals("读者") && !targetGroup.equals("管理员"))) {
                System.out.println("[AnnouncementRepository] ⚠️ TargetGroup值不符合约束: " + targetGroup + "，使用默认值: 所有人");
                validTargetGroup = "所有人";
            }
            ps.setString(5, validTargetGroup);
            ps.setString(6, "发布中");
                
                System.out.println("[AnnouncementRepository] 设置参数:");
                System.out.println("  参数1 (librarianid): " + librarianId);
                System.out.println("  参数2 (title): " + dto.getTitle());
                System.out.println("  参数3 (content): " + (dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
                System.out.println("  参数4 (createtime): " + now);
                System.out.println("  参数5 (targetgroup): " + targetGroup);
                System.out.println("  参数6 (status): 发布中");
                
                System.out.println("[AnnouncementRepository] 执行 INSERT 语句...");
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("[AnnouncementRepository] ✅ INSERT 执行成功");
                    
                    if (rs.next()) {
                        Integer generatedId = rs.getInt(1);
                        System.out.println("[AnnouncementRepository] ✅ 获取到生成的ID: " + generatedId);
                        
                        // 使用同一个连接查询，确保能看到刚插入的数据
                        System.out.println("[AnnouncementRepository] 使用同一连接查询刚插入的记录...");
                        Announcement result = findByIdWithConnection(conn, generatedId);
                        
                        if (result != null) {
                            System.out.println("[AnnouncementRepository] ✅ 查询到刚创建的公告:");
                            System.out.println("  AnnouncementID: " + result.getAnnouncementId());
                            System.out.println("  Title: " + result.getTitle());
                            System.out.println("  Status: " + result.getStatus());
                            System.out.println("  CreateTime: " + result.getCreateTime());
                            System.out.println("[AnnouncementRepository] ========== 创建公告成功 ==========");
                            return result;
                        } else {
                            System.out.println("[AnnouncementRepository] ⚠️ 查询刚创建的公告返回null，ID: " + generatedId);
                        }
                    } else {
                        System.out.println("[AnnouncementRepository] ⚠️ RETURNING 子句没有返回结果");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[AnnouncementRepository] ❌ SQL异常: " + e.getMessage());
            System.out.println("[AnnouncementRepository] SQL状态: " + e.getSQLState());
            System.out.println("[AnnouncementRepository] 错误代码: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("创建公告失败: " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("[AnnouncementRepository] ❌ 其他异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("创建公告失败: " + e.getMessage(), e);
        }
        
        System.out.println("[AnnouncementRepository] ❌ 创建公告失败，返回null");
        System.out.println("[AnnouncementRepository] ========== 创建公告完成 ==========");
        return null;
    }
    
    // 使用指定连接查找公告（用于创建后立即查询）
    private Announcement findByIdWithConnection(Connection conn, Integer id) throws SQLException {
        System.out.println("[AnnouncementRepository] findByIdWithConnection: 查询ID=" + id);
        String sql = "SELECT * FROM public.announcement WHERE announcementid=?";
        System.out.println("[AnnouncementRepository] 查询SQL: " + sql);
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            System.out.println("[AnnouncementRepository] 设置查询参数: id=" + id);
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("[AnnouncementRepository] 执行查询...");
                if (rs.next()) {
                    System.out.println("[AnnouncementRepository] ✅ 找到记录，开始映射...");
                    Announcement result = mapRow(rs);
                    System.out.println("[AnnouncementRepository] ✅ 映射完成");
                    return result;
                } else {
                    System.out.println("[AnnouncementRepository] ⚠️ 查询结果为空，未找到ID=" + id + "的记录");
                }
            }
        }
        return null;
    }

    // 更新公告
    public Announcement updateAnnouncement(Integer id, UpsertAnnouncementDto dto) {
        System.out.println("[AnnouncementRepository] ========== 开始更新公告 ==========");
        System.out.println("[AnnouncementRepository] AnnouncementID: " + id);
        System.out.println("[AnnouncementRepository] Title: " + dto.getTitle());
        System.out.println("[AnnouncementRepository] Content: " + (dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
        System.out.println("[AnnouncementRepository] TargetGroup: " + dto.getTargetGroup());
        
        // 如果前端没有提供 targetGroup，先查询原有记录获取
        String targetGroup = dto.getTargetGroup();
        if (targetGroup == null || targetGroup.trim().isEmpty()) {
            System.out.println("[AnnouncementRepository] TargetGroup为空，查询原有记录...");
            Announcement existing = findById(id);
            if (existing != null) {
                targetGroup = existing.getTargetGroup();
                System.out.println("[AnnouncementRepository] 从原有记录获取TargetGroup: " + targetGroup);
            } else {
                System.out.println("[AnnouncementRepository] ⚠️ 未找到原有记录，使用默认值: 所有人");
                targetGroup = "所有人";
            }
        }
        
        // 验证并修正 targetGroup 值
        if (!targetGroup.equals("所有人") && !targetGroup.equals("读者") && !targetGroup.equals("管理员")) {
            System.out.println("[AnnouncementRepository] ⚠️ TargetGroup值不符合约束: " + targetGroup + "，使用默认值: 所有人");
            targetGroup = "所有人";
        }
        
        String sql = "UPDATE public.announcement SET title=?, content=?, targetgroup=? WHERE announcementid=?";
        System.out.println("[AnnouncementRepository] SQL: " + sql);
        
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[AnnouncementRepository] ✅ 获取数据库连接成功");
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dto.getTitle());
                ps.setString(2, dto.getContent());
                ps.setString(3, targetGroup);
                ps.setInt(4, id);
                
                System.out.println("[AnnouncementRepository] 设置参数:");
                System.out.println("  参数1 (title): " + dto.getTitle());
                System.out.println("  参数2 (content): " + (dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
                System.out.println("  参数3 (targetgroup): " + targetGroup);
                System.out.println("  参数4 (announcementid): " + id);
                
                System.out.println("[AnnouncementRepository] 执行 UPDATE 语句...");
                int affected = ps.executeUpdate();
                System.out.println("[AnnouncementRepository] ✅ UPDATE 执行成功，影响行数: " + affected);
                
                if (affected > 0) {
                    System.out.println("[AnnouncementRepository] 查询更新后的记录...");
                    Announcement result = findById(id);
                    if (result != null) {
                        System.out.println("[AnnouncementRepository] ✅ 查询到更新后的公告:");
                        System.out.println("  AnnouncementID: " + result.getAnnouncementId());
                        System.out.println("  Title: " + result.getTitle());
                        System.out.println("  Status: " + result.getStatus());
                        System.out.println("[AnnouncementRepository] ========== 更新公告成功 ==========");
                        return result;
                    } else {
                        System.out.println("[AnnouncementRepository] ⚠️ 查询更新后的记录返回null");
                    }
                } else {
                    System.out.println("[AnnouncementRepository] ⚠️ 没有记录被更新，可能ID不存在");
                }
            }
        } catch (SQLException e) {
            System.out.println("[AnnouncementRepository] ❌ SQL异常: " + e.getMessage());
            System.out.println("[AnnouncementRepository] SQL状态: " + e.getSQLState());
            System.out.println("[AnnouncementRepository] 错误代码: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("更新公告失败: " + e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("[AnnouncementRepository] ❌ 其他异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("更新公告失败: " + e.getMessage(), e);
        }
        
        System.out.println("[AnnouncementRepository] ❌ 更新公告失败，返回null");
        System.out.println("[AnnouncementRepository] ========== 更新公告完成 ==========");
        return null;
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