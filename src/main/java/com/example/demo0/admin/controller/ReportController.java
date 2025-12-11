package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.HandleReportDto;
import com.example.demo0.admin.dto.ReportDetailDto;
import com.example.demo0.admin.dto.ReportDto;
import com.example.demo0.admin.service.ReportService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/api/admin/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "LIBRARIAN"}) // 限制只有管理员和图书管理员角色可以访问
public class ReportController {

    @Inject
    private ReportService reportService;
    
    private static final Logger logger = Logger.getLogger(ReportController.class.getName());

    @GET
    @Path("/pending")
    public Response getPendingReports(@QueryParam("librarianId") Integer librarianId) {
        try {
            logger.log(Level.INFO, "Fetching pending reports");
            List<ReportDetailDto> reports = reportService.getPendingReports();
            logger.log(Level.INFO, "Retrieved {0} pending reports", reports.size());
            return Response.ok(reports).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving pending reports", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new SimpleMessage("An error occurred while retrieving pending reports"))
                    .build();
        }
    }
    
    // 获取当前管理员ID
    private Integer getCurrentLibrarianId(@QueryParam("librarianId") Integer librarianId) {
        try {            
            // 从查询参数获取管理员ID
            logger.log(Level.INFO, "Received librarianId from query parameter: {0}", librarianId);
            if (librarianId == null) {
                throw new IllegalArgumentException("Librarian ID must be provided");
            }
            return librarianId;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get current librarian ID", e);
            throw new IllegalArgumentException("Librarian ID must be provided");
        }
    }

    @PUT
    @Path("/{reportId}")
    public Response handleReport(@PathParam("reportId") Integer reportId, HandleReportDto dto, @QueryParam("librarianId") Integer librarianId) {
        try {
            logger.log(Level.INFO, "Received request to handle report {0}", reportId);
            
            // 从路径参数设置reportId到DTO
            dto.setReportId(reportId);
            
            // 验证action字段是否已设置
            if (dto.getAction() == null || dto.getAction().trim().isEmpty()) {
                throw new IllegalArgumentException("处理动作(action)不能为空");
            }
            
            // 获取当前管理员ID
            Integer libId = getCurrentLibrarianId(librarianId);
            dto.setLibrarianId(libId);
            
            // 直接调用服务层处理举报，不再在控制器中设置状态
            boolean success = reportService.handleReport(dto);
            if (success) {
                logger.log(Level.INFO, "Report {0} handled successfully", reportId);
                return Response.noContent().build();
            }
            logger.log(Level.WARNING, "Failed to handle report {0}", reportId);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new SimpleMessage("Failed to handle the report."))
                    .build();
        } catch (IllegalArgumentException ex) {
            logger.log(Level.WARNING, "Bad request for report {0}: {1}", 
                new Object[]{reportId, ex.getMessage()});
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new SimpleMessage(ex.getMessage()))
                    .build();
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, "Runtime error handling report " + reportId, ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new SimpleMessage("An error occurred while processing your request"))
                    .build();
        }
    }

    @POST
    @Path("/add")
    public Response addReport(ReportDto report, @QueryParam("readerId") Integer readerId) {
        try {
            logger.log(Level.INFO, "Received request to add report for comment {0}", report.getCommentId());
            if (readerId == null) {
                throw new IllegalArgumentException("读者ID不能为空");
            }
            report.setReaderId(readerId);  // 设置举报人

            reportService.addReport(report);
            logger.log(Level.INFO, "Report added successfully");
            return Response.status(Response.Status.CREATED)
                    .entity(new SimpleMessage("举报添加成功")).build();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error adding report", ex);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new SimpleMessage("举报添加失败: " + ex.getMessage())).build();
        }
    }

    // 内部类用于返回简单消息
    private static class SimpleMessage {
        private String message;

        public SimpleMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}