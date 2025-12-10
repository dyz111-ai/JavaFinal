package com.example.demo0.reader.controller;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.reader.model.SeatReservation;
import com.example.demo0.reader.repository.SeatReservationRepository;
import com.example.demo0.reader.service.SeatReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "SpaceServlet", urlPatterns = {"/reader/space", "/reader/space/*"})
public class SpaceServlet extends HttpServlet {

    private final SeatReservationService seatService = new SeatReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String path = req.getPathInfo();
        if (path == null || path.isBlank() || "/".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/reader/space.jsp").forward(req, resp);
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");
        switch (path) {
            case "/seats":
                handleSeats(req, resp);
                break;
            case "/my-reservations":
                handleMyReservations(req, resp);
                break;
            case "/buildings":
                handleBuildings(resp);
                break;
            case "/floors":
                handleFloors(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json; charset=UTF-8");

        String path = req.getPathInfo();
        if ("/reservations/seat".equals(path)) {
            handleReserve(req, resp);
        } else if ("/reservations/cancel".equals(path)) {
            handleCancel(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleBuildings(HttpServletResponse resp) throws IOException {
        List<SeatReservationRepository.BuildingInfo> list = seatService.listBuildings();
        writeJson(resp, toJsonArray(list));
    }

    private void handleFloors(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int building = parseIntOrDefault(req.getParameter("buildingId"), 1);
        List<Integer> list = seatService.listFloors(building);
        writeJson(resp, toJsonArray(list));
    }

    private void handleSeats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int building = parseIntOrDefault(req.getParameter("buildingId"), 1);
        int floor = parseIntOrDefault(req.getParameter("floor"), 1);
        String dateStr = req.getParameter("date");
        String timeSlotStr = req.getParameter("timeSlot");
        
        if (dateStr == null || timeSlotStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"success\":false,\"message\":\"日期和时间段不能为空\"}");
            return;
        }
        
        List<SeatReservationService.SeatStatus> seats = seatService.getSeatLayout(building, floor, dateStr, timeSlotStr);
        writeJson(resp, toJsonArray(seats));
    }

    private void handleMyReservations(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, "{\"message\":\"未登录或登录已失效\"}");
            return;
        }
        List<SeatReservation> list = seatService.listMyReservations(currentUser.getReaderId());
        writeJson(resp, toJsonArray(list));
    }

    private void handleReserve(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, "{\"success\":false,\"message\":\"未登录或登录已失效\"}");
            return;
        }
        int building = parseIntOrDefault(req.getParameter("buildingId"), 1);
        int floor = parseIntOrDefault(req.getParameter("floor"), 1);
        String seatCode = req.getParameter("seatCode");
        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");
        
        if (seatCode == null || seatCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"success\":false,\"message\":\"seatCode 不能为空\"}");
            return;
        }
        
        if (startTimeStr == null || endTimeStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"success\":false,\"message\":\"开始时间和结束时间不能为空\"}");
            return;
        }
        
        try {
            // 解析ISO 8601格式的时间字符串
            java.time.Instant startInstant = java.time.Instant.parse(startTimeStr);
            java.time.Instant endInstant = java.time.Instant.parse(endTimeStr);
            java.time.LocalDateTime startTime = java.time.LocalDateTime.ofInstant(startInstant, java.time.ZoneId.systemDefault());
            java.time.LocalDateTime endTime = java.time.LocalDateTime.ofInstant(endInstant, java.time.ZoneId.systemDefault());
            seatService.reserve(building, floor, seatCode, currentUser, startTime, endTime);
            writeJson(resp, "{\"success\":true,\"message\":\"预约成功\"}");
        } catch (java.time.format.DateTimeParseException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"success\":false,\"message\":\"时间格式错误: " + escape(e.getMessage()) + "\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            writeJson(resp, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Reader currentUser = (Reader) req.getSession().getAttribute("currentUser");
        if (currentUser == null || currentUser.getReaderId() == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(resp, "{\"success\":false,\"message\":\"未登录或登录已失效\"}");
            return;
        }
        int building = parseIntOrDefault(req.getParameter("buildingId"), 1);
        int floor = parseIntOrDefault(req.getParameter("floor"), 1);
        String seatCode = req.getParameter("seatCode");
        if (seatCode == null || seatCode.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"success\":false,\"message\":\"seatCode 不能为空\"}");
            return;
        }
        try {
            boolean removed = seatService.cancel(building, floor, seatCode, currentUser.getReaderId());
            if (removed) {
                writeJson(resp, "{\"success\":true,\"message\":\"已取消预约\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeJson(resp, "{\"success\":false,\"message\":\"未找到该预约\"}");
            }
        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            writeJson(resp, "{\"success\":false,\"message\":\"" + escape(e.getMessage()) + "\"}");
        }
    }

    private int parseIntOrDefault(String val, int def) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return def;
        }
    }

    private void writeJson(HttpServletResponse resp, String json) throws IOException {
        try (PrintWriter out = resp.getWriter()) {
            out.write(json);
        }
    }

    private String toJsonArray(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            sb.append(toJsonObject(o));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonObject(Object o) {
        if (o instanceof SeatReservationService.SeatStatus s) {
            return "{\"buildingId\":" + s.buildingId +
                    ",\"floor\":" + s.floor +
                    ",\"seatCode\":\"" + escape(s.seatCode) + "\"" +
                    ",\"status\":\"" + s.status + "\"" +
                    ",\"readerId\":" + (s.readerId == null ? "null" : s.readerId) +
                    ",\"nickname\":" + (s.nickname == null ? "null" : "\"" + escape(s.nickname) + "\"") +
                    ",\"reservedAt\":" + (s.reservedAt == null ? "null" : "\"" + s.reservedAt + "\"") +
                    "}";
        }
        if (o instanceof Integer i) {
            return String.valueOf(i);
        }
        if (o instanceof SeatReservation r) {
            String reservedAtStr = r.getReservedAt() != null ? "\"" + r.getReservedAt().toString() + "\"" : "null";
            String endAtStr = r.getEndAt() != null ? "\"" + r.getEndAt().toString() + "\"" : "null";
            return "{\"buildingId\":" + r.getBuildingId() +
                    ",\"buildingName\":\"" + (r.getBuildingName() == null ? "" : escape(r.getBuildingName())) + "\"" +
                    ",\"floor\":" + r.getFloor() +
                    ",\"seatCode\":\"" + escape(r.getSeatCode()) + "\"" +
                    ",\"readerId\":" + r.getReaderId() +
                    ",\"nickname\":" + (r.getNickname() == null ? "null" : "\"" + escape(r.getNickname()) + "\"") +
                    ",\"reservedAt\":" + reservedAtStr +
                    ",\"endAt\":" + endAtStr +
                    ",\"status\":\"" + (r.getStatus() == null ? "" : escape(r.getStatus())) + "\"" +
                    "}";
        }
        if (o instanceof SeatReservationRepository.BuildingInfo b) {
            return "{\"buildingId\":" + b.buildingId +
                    ",\"buildingName\":\"" + escape(b.buildingName) + "\"" +
                    "}";
        }
        return "{}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "");
    }
}

