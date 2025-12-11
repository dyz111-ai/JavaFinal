package com.example.demo0.admin.dto;

import java.util.List;

public class PurchaseAnalysisDto {

    private List<BookRankingDto> topByBorrowCount;
    private List<BookRankingDto> topByBorrowDuration;
    private List<BookRankingDto> topByInstanceBorrow;

    // Getters and Setters
    public List<BookRankingDto> getTopByBorrowCount() { return topByBorrowCount; }
    public void setTopByBorrowCount(List<BookRankingDto> topByBorrowCount) { this.topByBorrowCount = topByBorrowCount; }

    public List<BookRankingDto> getTopByBorrowDuration() { return topByBorrowDuration; }
    public void setTopByBorrowDuration(List<BookRankingDto> topByBorrowDuration) { this.topByBorrowDuration = topByBorrowDuration; }

    public List<BookRankingDto> getTopByInstanceBorrow() { return topByInstanceBorrow; }
    public void setTopByInstanceBorrow(List<BookRankingDto> topByInstanceBorrow) { this.topByInstanceBorrow = topByInstanceBorrow; }
}
