package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.AnnouncementDto;
import com.example.demo0.admin.dto.UpsertAnnouncementDto;
import com.example.demo0.admin.entity.Announcement;
import com.example.demo0.admin.repository.AnnouncementRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnnouncementService {

    // 直接实例化，不再依赖容器注入
    private final AnnouncementRepository announcementRepository = new AnnouncementRepository();

    public List<AnnouncementDto> getAllAnnouncements() {
        return announcementRepository.getAllAnnouncements().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AnnouncementDto getAnnouncementById(Integer id) {
        return convertToDto(announcementRepository.findById(id));
    }

    public AnnouncementDto createAnnouncement(UpsertAnnouncementDto dto, Integer librarianId) {
        System.out.println("[AnnouncementService] ========== 开始创建公告 ==========");
        System.out.println("[AnnouncementService] 接收到的参数:");
        System.out.println("  LibrarianID: " + librarianId);
        System.out.println("  DTO: " + (dto != null ? "非空" : "null"));
        if (dto != null) {
            System.out.println("    Title: " + dto.getTitle());
            System.out.println("    Content: " + (dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
            System.out.println("    TargetGroup: " + dto.getTargetGroup());
        }
        
        System.out.println("[AnnouncementService] 调用 repository.createAnnouncement()...");
        Announcement entity = announcementRepository.createAnnouncement(dto, librarianId);
        
        if (entity != null) {
            System.out.println("[AnnouncementService] ✅ Repository返回实体:");
            System.out.println("  AnnouncementID: " + entity.getAnnouncementId());
            System.out.println("  Title: " + entity.getTitle());
            System.out.println("  Status: " + entity.getStatus());
            System.out.println("[AnnouncementService] 转换为DTO...");
            AnnouncementDto result = convertToDto(entity);
            System.out.println("[AnnouncementService] ✅ 转换完成");
            System.out.println("[AnnouncementService] ========== 创建公告成功 ==========");
            return result;
        } else {
            System.out.println("[AnnouncementService] ❌ Repository返回null");
            System.out.println("[AnnouncementService] ========== 创建公告失败 ==========");
            return null;
        }
    }

    public AnnouncementDto updateAnnouncement(Integer id, UpsertAnnouncementDto dto) {
        System.out.println("[AnnouncementService] ========== 开始更新公告 ==========");
        System.out.println("[AnnouncementService] AnnouncementID: " + id);
        System.out.println("[AnnouncementService] 接收到的DTO:");
        if (dto != null) {
            System.out.println("  Title: " + dto.getTitle());
            System.out.println("  Content: " + (dto.getContent() != null ? "长度=" + dto.getContent().length() : "null"));
            System.out.println("  TargetGroup: " + dto.getTargetGroup());
        } else {
            System.out.println("  DTO: null");
        }
        
        System.out.println("[AnnouncementService] 调用 repository.updateAnnouncement()...");
        Announcement entity = announcementRepository.updateAnnouncement(id, dto);
        
        if (entity != null) {
            System.out.println("[AnnouncementService] ✅ Repository返回实体:");
            System.out.println("  AnnouncementID: " + entity.getAnnouncementId());
            System.out.println("  Title: " + entity.getTitle());
            System.out.println("  Status: " + entity.getStatus());
            System.out.println("[AnnouncementService] 转换为DTO...");
            AnnouncementDto result = convertToDto(entity);
            System.out.println("[AnnouncementService] ✅ 转换完成");
            System.out.println("[AnnouncementService] ========== 更新公告成功 ==========");
            return result;
        } else {
            System.out.println("[AnnouncementService] ❌ Repository返回null");
            System.out.println("[AnnouncementService] ========== 更新公告失败 ==========");
            return null;
        }
    }

    public boolean takedownAnnouncement(Integer id) {
        return announcementRepository.updateStatus(id, "已撤回");
    }

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
}