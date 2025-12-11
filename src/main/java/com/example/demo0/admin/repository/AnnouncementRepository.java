package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Announcement;
import com.example.demo0.admin.dto.UpsertAnnouncementDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AnnouncementRepository {
    private static final Logger logger = Logger.getLogger(AnnouncementRepository.class.getName());
    
    // 常量定义
    public static final String ANNOUNCEMENT_STATUS_ACTIVE = "发布中";
    public static final String ANNOUNCEMENT_STATUS_INACTIVE = "已撤回";

    @PersistenceContext
    private EntityManager entityManager;

    public List<Announcement> getAllAnnouncements() {
        try {
            String jpql = "SELECT a FROM Announcement a ORDER BY a.createTime DESC";
            return entityManager.createQuery(jpql, Announcement.class).getResultList();
        } catch (Exception e) {
            logger.error("获取所有公告失败", e);
            return List.of();
        }
    }

    public List<Announcement> getPublicAnnouncements() {
        try {
            String jpql = "SELECT a FROM Announcement a WHERE a.status = :status ORDER BY a.createTime DESC";
            return entityManager.createQuery(jpql, Announcement.class)
                    .setParameter("status", ANNOUNCEMENT_STATUS_ACTIVE)
                    .getResultList();
        } catch (Exception e) {
            logger.error("获取公开公告失败", e);
            return List.of();
        }
    }

    public Announcement createAnnouncement(UpsertAnnouncementDto dto, Integer librarianId) {
        // 参数验证（使用更详细的错误信息）
        if (dto == null) {
            throw new IllegalArgumentException("公告数据不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }
        if (librarianId == null) {
            throw new IllegalArgumentException("管理员ID不能为空");
        }
        
        try {
            Announcement announcement = new Announcement();
            announcement.setLibrarianId(librarianId);
            announcement.setTitle(dto.getTitle());
            announcement.setContent(dto.getContent());
            announcement.setTargetGroup(dto.getTargetGroup());
            announcement.setStatus(ANNOUNCEMENT_STATUS_ACTIVE);
            announcement.setCreateTime(LocalDateTime.now());

            entityManager.persist(announcement);
            logger.infof("创建公告成功: id=%d, title=%s", announcement.getAnnouncementId(), announcement.getTitle());
            return announcement;
        } catch (Exception e) {
            logger.error("创建公告失败", e);
            throw new RuntimeException("创建公告失败: " + e.getMessage(), e);
        }
    }

    public Announcement updateAnnouncement(Integer id, UpsertAnnouncementDto dto) {
        // 参数验证
        if (id == null) {
            throw new IllegalArgumentException("公告ID不能为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("公告数据不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("公告标题不能为空");
        }

        try {
            Announcement announcement = entityManager.find(Announcement.class, id);
            if (announcement == null) {
                logger.warn("公告不存在: " + id);
                throw new NoResultException("公告不存在: " + id);
            }

            announcement.setTitle(dto.getTitle());
            announcement.setContent(dto.getContent());
            announcement.setTargetGroup(dto.getTargetGroup());
            entityManager.merge(announcement);
            logger.infof("更新公告成功: id=%d, title=%s", id, dto.getTitle());
            return announcement;
        } catch (NoResultException e) {
            throw e; // 直接抛出，不需要额外包装
        } catch (Exception e) {
            logger.error("更新公告失败: " + id, e);
            throw new RuntimeException("更新公告失败: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(Integer id, String status) {
        // 参数验证
        if (id == null) {
            throw new IllegalArgumentException("公告ID不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("公告状态不能为空");
        }
        // 验证状态值是否有效
        if (!ANNOUNCEMENT_STATUS_ACTIVE.equals(status) && !ANNOUNCEMENT_STATUS_INACTIVE.equals(status)) {
            throw new IllegalArgumentException("无效的公告状态: " + status);
        }

        try {
            Announcement announcement = entityManager.find(Announcement.class, id);
            if (announcement == null) {
                logger.warn("公告不存在: " + id);
                return false;
            }

            announcement.setStatus(status);
            entityManager.merge(announcement);
            logger.infof("更新公告状态成功: id=%d, status=%s", id, status);
            return true;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("更新公告状态失败: " + id, e);
            return false;
        }
    }

    public Announcement findById(Integer id) {
        try {
            return entityManager.find(Announcement.class, id);
        } catch (Exception e) {
            logger.error("查找公告失败: " + id, e);
            return null;
        }
    }
}