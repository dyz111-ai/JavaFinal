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
        return convertToDto(announcementRepository.createAnnouncement(dto, librarianId));
    }

    public AnnouncementDto updateAnnouncement(Integer id, UpsertAnnouncementDto dto) {
        return convertToDto(announcementRepository.updateAnnouncement(id, dto));
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