package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.BookRankingDto;
import com.example.demo0.admin.dto.PurchaseAnalysisDto;
import com.example.demo0.admin.dto.PurchaseLogDto;
import com.example.demo0.admin.entity.Purchaselog;
import com.example.demo0.admin.repository.PurchaseAnalysisRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
@Transactional
public class PurchaseAnalysisService {

    private static final Logger LOGGER = Logger.getLogger(PurchaseAnalysisService.class);
    
    @Inject
    private PurchaseAnalysisRepository purchaseAnalysisRepository;

    private BookRankingDto convertToBookRankingDto(Object[] record) {
        if (record == null || record.length < 4) {
            LOGGER.warn("Invalid book ranking data format, record is null or has insufficient fields");
            return null;
        }

        BookRankingDto dto = new BookRankingDto();
        dto.setIsbn(record[0] != null ? record[0].toString() : "");
        dto.setTitle(record[1] != null ? record[1].toString() : "");
        dto.setAuthor(record[2] != null ? record[2].toString() : "");

        if (record[3] instanceof Number) {
            dto.setMetricValue(((Number) record[3]).intValue());
        } else {
            try {
                dto.setMetricValue(Integer.parseInt(record[3].toString()));
            } catch (NumberFormatException e) {
                LOGGER.error("Failed to parse metric value: " + record[3], e);
                dto.setMetricValue(0);
            }
        }
        return dto;
    }

    private PurchaseLogDto convertToPurchaseLogDto(Purchaselog entity) {
        if (entity == null) {
            LOGGER.warn("Attempting to convert null purchase log entity");
            return null;
        }

        PurchaseLogDto dto = new PurchaseLogDto();
        dto.setLogId(entity.getLogId());
        dto.setLogText(entity.getLogText());
        dto.setLogDate(entity.getLogDate());
        dto.setAdminId(entity.getAdminId()); // 现在entity.getAdminId()返回Integer类型，与dto的类型匹配
        return dto;
    }

    public PurchaseAnalysisDto getPurchaseAnalysis() {
        try {
            LOGGER.info("获取购买分析数据");
            
            PurchaseAnalysisDto analysis = new PurchaseAnalysisDto();

            List<Object[]> borrowCountData = purchaseAnalysisRepository.getTop10ByBorrowCount();
            analysis.setTopByBorrowCount(borrowCountData.stream()
                    .map(this::convertToBookRankingDto)
                    .filter(dto -> dto != null) // 过滤掉转换失败的记录
                    .collect(Collectors.toList()));

            List<Object[]> borrowDurationData = purchaseAnalysisRepository.getTop10ByBorrowDuration();
            analysis.setTopByBorrowDuration(borrowDurationData.stream()
                    .map(this::convertToBookRankingDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList()));

            List<Object[]> instanceBorrowData = purchaseAnalysisRepository.getTop10ByInstanceBorrow();
            analysis.setTopByInstanceBorrow(instanceBorrowData.stream()
                    .map(this::convertToBookRankingDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList()));

            LOGGER.info("获取购买分析数据成功");
            return analysis;
        } catch (Exception e) {
            LOGGER.error("获取购买分析数据失败", e);
            throw new RuntimeException("获取购买分析数据失败", e);
        }
    }

    public List<PurchaseLogDto> getPurchaseLogs() {
        try {
            LOGGER.info("获取购买日志列表");
            
            List<Purchaselog> logs = purchaseAnalysisRepository.getPurchaseLogs();
            return logs.stream()
                    .map(this::convertToPurchaseLogDto)
                    .filter(dto -> dto != null) // 过滤掉转换失败的记录
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("获取购买日志列表失败", e);
            throw new RuntimeException("获取购买日志列表失败", e);
        }
    }

    public void addPurchaseLog(String logText, Integer adminId) {
        try {
            LOGGER.info("添加购买日志，管理员ID: " + adminId);
            
            // 验证参数
            if (adminId == null) {
                throw new IllegalArgumentException("管理员ID不能为空");
            }
            
            // 确保日志文本不为null
            if (logText == null) {
                logText = "";
            }
            
            purchaseAnalysisRepository.addPurchaseLog(logText, adminId);
            LOGGER.info("购买日志添加成功，管理员ID: " + adminId);
        } catch (IllegalArgumentException e) {
            LOGGER.error("添加购买日志参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("添加购买日志失败", e);
            throw new RuntimeException("添加购买日志失败", e);
        }
    }
}