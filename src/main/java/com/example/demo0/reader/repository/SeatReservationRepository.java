package com.example.demo0.reader.repository;

import com.example.demo0.reader.model.SeatReservation;
import com.example.demo0.reader.service.SeatReservationService.SeatStatus;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 座位预约仓储层，对应 schema_new.sql 中的 Seat / Reserve_Seat 表。
 */
public class SeatReservationRepository {

    private final DataSource dataSource;

    public SeatReservationRepository() {
        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/jdbc/LibraryDS");
        } catch (NamingException e) {
            throw new RuntimeException("数据源查找失败: " + e.getMessage(), e);
        }
    }

    public List<SeatStatus> findSeatLayout(int buildingId, int floor, String date, String timeSlot) {
        // 解析时间段：8-10, 10-12, 14-16, 16-18
        int startHour, endHour;
        switch (timeSlot) {
            case "8-10":
                startHour = 8;
                endHour = 10;
                break;
            case "10-12":
                startHour = 10;
                endHour = 12;
                break;
            case "14-16":
                startHour = 14;
                endHour = 16;
                break;
            case "16-18":
                startHour = 16;
                endHour = 18;
                break;
            default:
                startHour = 8;
                endHour = 10;
        }
        
        // 验证日期格式
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException("日期不能为空");
        }
        
        // 构建开始和结束时间，确保格式正确：yyyy-MM-dd HH:mm:ss
        String startTimeStr = date + " " + String.format("%02d:00:00", startHour);
        String endTimeStr = date + " " + String.format("%02d:00:00", endHour);
        
        // 验证时间格式
        try {
            Timestamp.valueOf(startTimeStr);
            Timestamp.valueOf(endTimeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("日期格式错误: " + date + ", 期望格式: yyyy-MM-dd", e);
        }
        
        // 查询指定时间段内被预约的座位
        // 时间段重叠判断：预约的开始时间 < 查询的结束时间 AND 预约的结束时间 > 查询的开始时间
        String sql = """
                SELECT s.SeatNumber, s.BuildingID, s.Floor,
                       active_rs.ReaderID, r.Nickname, active_rs.StartTime, active_rs.EndTime
                FROM public.Seat s
                LEFT JOIN (
                    SELECT SeatID, ReaderID, StartTime, EndTime
                    FROM public.Reserve_Seat
                    WHERE Status = '未完成'
                      AND StartTime < ? AND EndTime > ?
                ) active_rs ON active_rs.SeatID = s.SeatID
                LEFT JOIN public.Reader r ON active_rs.ReaderID = r.ReaderID
                WHERE s.BuildingID = ? AND s.Floor = ?
                ORDER BY s.SeatNumber
                """;
        List<SeatStatus> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(endTimeStr));
            ps.setTimestamp(2, Timestamp.valueOf(startTimeStr));
            ps.setInt(3, buildingId);
            ps.setInt(4, floor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeatStatus status = new SeatStatus();
                    status.buildingId = rs.getInt("BuildingID");
                    status.floor = rs.getInt("Floor");
                    status.seatCode = rs.getString("SeatNumber");
                    // 只有在该时间段内被预约的座位才显示为已预约
                    Integer readerId = (Integer) rs.getObject("ReaderID");
                    status.readerId = readerId;
                    status.nickname = rs.getString("Nickname");
                    status.status = (readerId != null) ? "reserved" : "free";
                    Timestamp startTime = rs.getTimestamp("StartTime");
                    if (startTime != null) {
                        status.reservedAt = startTime.toLocalDateTime();
                    }
                    list.add(status);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询座位布局失败: " + e.getMessage(), e);
        }
        return list;
    }

    public void reserve(int buildingId, int floor, String seatNumber, int readerId, String nickname,
                        LocalDateTime start, LocalDateTime end) {
        String selectSeat = "SELECT SeatID, ReservationStatus FROM public.Seat WHERE BuildingID=? AND Floor=? AND SeatNumber=? FOR UPDATE";
        String checkReaderActive = "SELECT COUNT(*) FROM public.Reserve_Seat WHERE ReaderID=? AND Status='未完成'";
        String checkSeatConflict = "SELECT COUNT(*) FROM public.Reserve_Seat WHERE SeatID=? AND Status='未完成' AND StartTime < ? AND EndTime > ?";
        String insertReserve = "INSERT INTO public.Reserve_Seat (ReaderID, SeatID, ReservationTime, StartTime, EndTime, Status) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, '未完成')";
        String updateSeat = "UPDATE public.Seat SET ReservationStatus='已预约' WHERE SeatID=?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 步骤1: 检查读者是否已有未完成的预约
                try (PreparedStatement checkReader = conn.prepareStatement(checkReaderActive)) {
                    checkReader.setInt(1, readerId);
                    try (ResultSet rs = checkReader.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            throw new IllegalStateException("您已有有效的未完成预约，无法再次预约");
                        }
                    }
                }
                
                // 步骤2: 查询座位ID并加锁
                int seatId;
                try (PreparedStatement ps = conn.prepareStatement(selectSeat)) {
                    ps.setInt(1, buildingId);
                    ps.setInt(2, floor);
                    ps.setString(3, seatNumber);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            throw new IllegalArgumentException("座位不存在");
                        }
                        seatId = rs.getInt("SeatID");
                        String status = rs.getString("ReservationStatus");
                        if (!"空闲".equals(status)) {
                            conn.rollback();
                            throw new IllegalStateException("该座位已被预约");
                        }
                    }
                }
                
                // 步骤3: 检查座位在该时间段是否已被占用（时间冲突检查）
                try (PreparedStatement checkConflict = conn.prepareStatement(checkSeatConflict)) {
                    checkConflict.setInt(1, seatId);
                    checkConflict.setTimestamp(2, Timestamp.valueOf(end));
                    checkConflict.setTimestamp(3, Timestamp.valueOf(start));
                    try (ResultSet rs = checkConflict.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            throw new IllegalStateException("该座位在此时间段已被预约，请选择其他时间");
                        }
                    }
                }
                
                // 步骤4: 插入预约记录
                try (PreparedStatement insert = conn.prepareStatement(insertReserve)) {
                    insert.setInt(1, readerId);
                    insert.setInt(2, seatId);
                    insert.setTimestamp(3, Timestamp.valueOf(start));
                    insert.setTimestamp(4, Timestamp.valueOf(end));
                    insert.executeUpdate();
                }
                
                // 步骤5: 更新座位状态
                try (PreparedStatement upd = conn.prepareStatement(updateSeat)) {
                    upd.setInt(1, seatId);
                    upd.executeUpdate();
                }
                
                conn.commit();
            } catch (IllegalArgumentException | IllegalStateException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("预约座位失败: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("预约座位失败: " + e.getMessage(), e);
        }
    }

    public boolean cancel(int buildingId, int floor, String seatNumber, int readerId) {
        String selectSeat = "SELECT SeatID FROM public.Seat WHERE BuildingID=? AND Floor=? AND SeatNumber=? FOR UPDATE";
        String cancelSql = "UPDATE public.Reserve_Seat SET Status='取消', EndTime=CURRENT_TIMESTAMP WHERE SeatID=? AND ReaderID=? AND Status='未完成'";
        String freeSeat = "UPDATE public.Seat SET ReservationStatus='空闲' WHERE SeatID=?";
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(selectSeat)) {
                ps.setInt(1, buildingId);
                ps.setInt(2, floor);
                ps.setString(3, seatNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    int seatId = rs.getInt("SeatID");
                    int updated;
                    try (PreparedStatement cancel = conn.prepareStatement(cancelSql)) {
                        cancel.setInt(1, seatId);
                        cancel.setInt(2, readerId);
                        updated = cancel.executeUpdate();
                    }
                    if (updated > 0) {
                        try (PreparedStatement free = conn.prepareStatement(freeSeat)) {
                            free.setInt(1, seatId);
                            free.executeUpdate();
                        }
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("取消预约失败: " + e.getMessage(), e);
        }
    }

    public List<SeatReservation> findByReader(int readerId) {
        String sql = """
                SELECT rs.ReservationID, rs.StartTime, rs.EndTime, rs.Status,
                       s.BuildingID, COALESCE(b.BuildingName, '楼栋' || s.BuildingID) AS BuildingName,
                       s.Floor, s.SeatNumber
                FROM public.Reserve_Seat rs
                JOIN public.Seat s ON rs.SeatID = s.SeatID
                LEFT JOIN public.Building b ON s.BuildingID = b.BuildingID
                WHERE rs.ReaderID = ?
                ORDER BY rs.ReservationTime DESC
                """;
        List<SeatReservation> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, readerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SeatReservation r = new SeatReservation();
                    r.setBuildingId(rs.getInt("BuildingID"));
                    r.setBuildingName(rs.getString("BuildingName"));
                    r.setFloor(rs.getInt("Floor"));
                    r.setSeatCode(rs.getString("SeatNumber"));
                    Timestamp startTime = rs.getTimestamp("StartTime");
                    if (startTime != null) {
                        r.setReservedAt(startTime.toLocalDateTime());
                    }
                    Timestamp end = rs.getTimestamp("EndTime");
                    if (end != null) {
                        r.setEndAt(end.toLocalDateTime());
                    }
                    r.setStatus(rs.getString("Status"));
                    r.setReaderId(readerId);
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询我的预约失败: " + e.getMessage(), e);
        }
        return list;
    }

    public static class BuildingInfo {
        public int buildingId;
        public String buildingName;
    }

    public List<BuildingInfo> listBuildings() {
        String sql = """
                SELECT DISTINCT s.BuildingID, COALESCE(b.BuildingName, '楼栋' || s.BuildingID) AS BuildingName
                FROM public.Seat s
                LEFT JOIN public.Building b ON s.BuildingID = b.BuildingID
                ORDER BY s.BuildingID
                """;
        List<BuildingInfo> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BuildingInfo info = new BuildingInfo();
                info.buildingId = rs.getInt("BuildingID");
                info.buildingName = rs.getString("BuildingName");
                list.add(info);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询楼栋失败: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Integer> listFloors(int buildingId) {
        String sql = "SELECT DISTINCT Floor FROM public.Seat WHERE BuildingID = ? ORDER BY Floor";
        List<Integer> floors = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, buildingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    floors.add(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询楼层失败: " + e.getMessage(), e);
        }
        return floors;
    }
}

