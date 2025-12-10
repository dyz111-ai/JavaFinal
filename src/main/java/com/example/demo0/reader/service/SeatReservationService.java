package com.example.demo0.reader.service;

import com.example.demo0.auth.model.Reader;
import com.example.demo0.reader.model.SeatReservation;
import com.example.demo0.reader.repository.SeatReservationRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 座位预约服务（持久化版）。
 */
public class SeatReservationService {

    private final SeatReservationRepository repository = new SeatReservationRepository();

    public List<SeatStatus> getSeatLayout(int buildingId, int floor, String date, String timeSlot) {
        return repository.findSeatLayout(buildingId, floor, date, timeSlot);
    }

    public void reserve(int buildingId, int floor, String seatCode, Reader reader, LocalDateTime startTime, LocalDateTime endTime) {
        // 时间校验
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
        
        // 固定时间段为2小时，不需要再校验时长范围
        // 调用Repository进行预约（包含读者未完成预约检查和时间段冲突检查）
        repository.reserve(buildingId, floor, seatCode, reader.getReaderId(), reader.getNickname(), startTime, endTime);
    }

    public boolean cancel(int buildingId, int floor, String seatCode, Integer readerId) {
        return repository.cancel(buildingId, floor, seatCode, readerId);
    }

    public List<SeatReservation> listMyReservations(Integer readerId) {
        return repository.findByReader(readerId);
    }

    public List<SeatReservationRepository.BuildingInfo> listBuildings() {
        return repository.listBuildings();
    }

    public List<Integer> listFloors(int buildingId) {
        return repository.listFloors(buildingId);
    }

    public static class SeatStatus {
        public int buildingId;
        public int floor;
        public String seatCode;
        public String status; // free | reserved
        public Integer readerId;
        public String nickname;
        public LocalDateTime reservedAt;
    }
}

