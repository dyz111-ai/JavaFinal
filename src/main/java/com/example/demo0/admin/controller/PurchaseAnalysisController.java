package com.example.demo0.admin.controller;

import com.example.demo0.admin.dto.CreatePurchaseLogDto;
import com.example.demo0.admin.dto.PurchaseAnalysisDto;
import com.example.demo0.admin.dto.PurchaseLogDto;
import com.example.demo0.admin.service.PurchaseAnalysisService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/admin/purchase-analysis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN"}) // 分析数据仅限管理员访问
public class PurchaseAnalysisController {

    @Inject
    private PurchaseAnalysisService purchaseAnalysisService;

    @GET
    public Response getAnalysis() {
        PurchaseAnalysisDto analysis = purchaseAnalysisService.getPurchaseAnalysis();
        return Response.ok(analysis).build();
    }

    @GET
    @Path("/logs")
    public Response getLogs() {
        List<PurchaseLogDto> logs = purchaseAnalysisService.getPurchaseLogs();
        return Response.ok(logs).build();
    }

    @POST
    @Path("/logs")
    public Response addLog(CreatePurchaseLogDto dto) {
        if (dto.getLogText() == null || dto.getLogText().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new SimpleMessage("Log text cannot be empty."))
                    .build();
        }

        purchaseAnalysisService.addPurchaseLog(dto.getLogText(), dto.getAdminId());
        return Response.status(Response.Status.CREATED).build();
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