package com.example.demo0.admin.dto;

import java.util.ArrayList;
import java.util.List;

public class PublicAnnouncementsDto {

    private List<AnnouncementDto> urgent = new ArrayList<>();
    private List<AnnouncementDto> regular = new ArrayList<>();

    // Getters and Setters
    public List<AnnouncementDto> getUrgent() { return urgent; }
    public void setUrgent(List<AnnouncementDto> urgent) { this.urgent = urgent; }

    public List<AnnouncementDto> getRegular() { return regular; }
    public void setRegular(List<AnnouncementDto> regular) { this.regular = regular; }
}
