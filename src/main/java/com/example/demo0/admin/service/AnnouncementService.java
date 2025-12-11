package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.AnnouncementDto;
import com.example.demo0.admin.dto.PublicAnnouncementsDto;
import com.example.demo0.admin.dto.UpsertAnnouncementDto;
import com.example.demo0.admin.entity.Announcement;
import com.example.demo0.admin.repository.AnnouncementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

//公告状态常量
import static com.example.demo0.admin.repository.AnnouncementRepository.ANNOUNCEMENT_STATUS_ACTIVE;
import static com.example.demo0.admin.repository.AnnouncementRepository.ANNOUNCEMENT_STATUS_INACTIVE;

@ApplicationScoped
@Transactional
public class AnnouncementService {

    private static final Logger LOGGER = Logger.getLogger(AnnouncementService.class);
    
    @Inject
    private AnnouncementRepository announcementRepository;

    private AnnouncementDto convertToDto(Announcement entity) {
        if (entity == null) return null;

        AnnouncementDto dto = new AnnouncementDto();
        dto.setAnnouncementId(entity.getAnnouncementId());
        dto.setLibrarianId(entity.getLibrarianId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCreateTime(entity.getCreateTime());
        dto.setTargetGroup(entity.getTargetGroup());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    /**
     * 获取所有公告（包括已下线的）
     * 供管理员使用的方法
     */
    public List<AnnouncementDto> getAllAnnouncements() {
        try {
            LOGGER.info("获取所有公告列表");
            List<Announcement> announcements = announcementRepository.getAllAnnouncements();
            return announcements.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("获取公告列表失败", e);
            throw new RuntimeException("获取公告列表失败", e);
        }
    }
    
    /**
     * 根据ID获取单个公告
     * 供管理员编辑公告使用
     */
    public AnnouncementDto getAnnouncementById(Integer id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("公告ID不能为空");
            }
            
            LOGGER.info("获取公告详情，ID: " + id);
            Announcement announcement = announcementRepository.findById(id);
            return convertToDto(announcement);
        } catch (IllegalArgumentException e) {
            LOGGER.error("获取公告详情参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("获取公告详情失败", e);
            throw new RuntimeException("获取公告详情失败", e);
        }
    }

    public PublicAnnouncementsDto getPublicAnnouncements() {
        try {
            LOGGER.info("获取公开公告列表");
            List<Announcement> allPublic = announcementRepository.getPublicAnnouncements();
            PublicAnnouncementsDto result = new PublicAnnouncementsDto();
            
            // 区分紧急公告和普通公告
            // 紧急公告判断标准：标题包含"紧急"、"重要"等关键词，或targetGroup为特定值
            result.setUrgent(allPublic.stream()
                    .filter(announcement -> 
                        announcement.getTitle().contains("紧急") || 
                        announcement.getTitle().contains("重要") || 
                        "紧急".equals(announcement.getTargetGroup())
                    )
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
            
            // 普通公告：排除紧急公告，取最新的3条
            result.setRegular(allPublic.stream()
                    .filter(announcement -> 
                        !announcement.getTitle().contains("紧急") && 
                        !announcement.getTitle().contains("重要") && 
                        !"紧急".equals(announcement.getTargetGroup())
                    )
                    .limit(3)
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
            
            return result;
        } catch (Exception e) {
            LOGGER.error("获取公开公告列表失败", e);
            throw new RuntimeException("获取公开公告列表失败", e);
        }
    }

    public AnnouncementDto createAnnouncement(UpsertAnnouncementDto dto, Integer librarianId) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("公告数据不能为空");
            }
            
            if (librarianId == null) {
                throw new IllegalArgumentException("图书馆员ID不能为空");
            }
            
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("公告标题不能为空");
            }
            
            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("公告内容不能为空");
            }
            
            LOGGER.info("创建新公告: " + dto.getTitle());
            
            Announcement announcement = announcementRepository.createAnnouncement(dto, librarianId);
            LOGGER.info("公告创建成功，ID: " + announcement.getAnnouncementId());
            return convertToDto(announcement);
        } catch (IllegalArgumentException e) {
            LOGGER.error("创建公告参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("创建公告失败", e);
            throw new RuntimeException("创建公告失败", e);
        }
    }

    public AnnouncementDto updateAnnouncement(Integer id, UpsertAnnouncementDto dto) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("公告ID不能为空");
            }
            
            if (dto == null) {
                throw new IllegalArgumentException("公告数据不能为空");
            }
            
            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("公告标题不能为空");
            }
            
            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("公告内容不能为空");
            }
            
            LOGGER.info("更新公告，ID: " + id);
            
            Announcement announcement = announcementRepository.updateAnnouncement(id, dto);
            LOGGER.info("公告更新成功，ID: " + announcement.getAnnouncementId());
            return convertToDto(announcement);
        } catch (IllegalArgumentException e) {
            LOGGER.error("更新公告参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("更新公告失败", e);
            throw new RuntimeException("更新公告失败", e);
        }
    }

    public boolean takedownAnnouncement(Integer id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("公告ID不能为空");
            }
            
            LOGGER.info("下架公告，ID: " + id);
            
            boolean result = announcementRepository.updateStatus(id, ANNOUNCEMENT_STATUS_INACTIVE);
            LOGGER.info("公告下架" + (result ? "成功" : "失败") + "，ID: " + id);
            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.error("下架公告参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("下架公告失败", e);
            throw new RuntimeException("下架公告失败", e);
        }
    }
}