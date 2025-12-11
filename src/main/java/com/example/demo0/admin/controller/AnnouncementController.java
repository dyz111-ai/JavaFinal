package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.AnnouncementDto;
import com.example.demo0.admin.dto.PublicAnnouncementsDto;
import com.example.demo0.admin.dto.UpsertAnnouncementDto;
import com.example.demo0.admin.service.AnnouncementService;
import jakarta.inject.Inject;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "LIBRARIAN"}) // 限制只有管理员和图书管理员角色可以访问
public class AnnouncementController {

    @Inject
    private AnnouncementService announcementService;

    // Public endpoint for homepage
    @GET
    @Path("/announcements/public")
    public Response getPublicAnnouncements() {
        PublicAnnouncementsDto result = announcementService.getPublicAnnouncements();
        return Response.ok(result).build();
    }

    // Admin endpoints
    @GET
    @Path("/admin/announcements")
    public Response getAllAnnouncements() {
        List<AnnouncementDto> announcements = announcementService.getAllAnnouncements();
        return Response.ok(announcements).build();
    }
    
    @GET
    @Path("/admin/announcements/{id}")
    public Response getAnnouncementById(@PathParam("id") Integer id) {
        AnnouncementDto announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(announcement).build();
    }

    @POST
    @Path("/admin/announcements")
    public Response createAnnouncement(UpsertAnnouncementDto dto, @QueryParam("adminId") Integer adminId) {
        // In a real application, adminId should be obtained from authentication context
        // For example: SecurityContext securityContext = getSecurityContext();
        // Integer adminId = ((UserPrincipal)securityContext.getUserPrincipal()).getUserId();
        AnnouncementDto created = announcementService.createAnnouncement(dto, adminId);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    @PUT
    @Path("/admin/announcements/{id}")
    public Response updateAnnouncement(@PathParam("id") Integer id, UpsertAnnouncementDto dto) {
        AnnouncementDto updated = announcementService.updateAnnouncement(id, dto);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/admin/announcements/{id}/takedown")
    public Response takedownAnnouncement(@PathParam("id") Integer id) {
        boolean success = announcementService.takedownAnnouncement(id);
        if (!success) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}