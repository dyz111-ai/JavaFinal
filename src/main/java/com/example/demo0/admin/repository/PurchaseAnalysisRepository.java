package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Purchaselog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PurchaseAnalysisRepository {
    // 添加日志记录器
    private static final Logger logger = Logger.getLogger(PurchaseAnalysisRepository.class.getName());
    
    // SQL查询常量 - 直接查询表，不使用视图
    // 1. 借阅次数排行：统计每个ISBN的借阅次数
    private static final String SQL_TOP_10_BY_BORROW_COUNT = 
        "SELECT bi.isbn, bi.title, bi.author, COUNT(br.borrowrecordid) AS metric_value " +
        "FROM public.borrowrecord br " +
        "JOIN public.book b ON br.bookid = b.bookid " +
        "JOIN public.bookinfo bi ON b.isbn = bi.isbn " +
        "GROUP BY bi.isbn, bi.title, bi.author " +
        "ORDER BY metric_value DESC " +
        "LIMIT 10";
    
    // 2. 借阅时长排行：统计每个ISBN的总借阅天数（只计算已归还的）
    private static final String SQL_TOP_10_BY_BORROW_DURATION = 
        "SELECT bi.isbn, bi.title, bi.author, " +
        "       COALESCE(ROUND(SUM(EXTRACT(EPOCH FROM (br.returntime - br.borrowtime)) / 86400))::INTEGER, 0) AS metric_value " +
        "FROM public.borrowrecord br " +
        "JOIN public.book b ON br.bookid = b.bookid " +
        "JOIN public.bookinfo bi ON b.isbn = bi.isbn " +
        "WHERE br.returntime IS NOT NULL " +
        "GROUP BY bi.isbn, bi.title, bi.author " +
        "ORDER BY metric_value DESC " +
        "LIMIT 10";
    
    // 3. 实例借阅排行：统计每个Barcode（单本图书）的借阅次数
    private static final String SQL_TOP_10_BY_INSTANCE_BORROW = 
        "SELECT b.barcode AS isbn, bi.title, bi.author, COUNT(br.borrowrecordid) AS metric_value " +
        "FROM public.borrowrecord br " +
        "JOIN public.book b ON br.bookid = b.bookid " +
        "JOIN public.bookinfo bi ON b.isbn = bi.isbn " +
        "GROUP BY b.barcode, bi.title, bi.author " +
        "ORDER BY metric_value DESC " +
        "LIMIT 10";
    
    // 获取采购日志的原生SQL查询
    private static final String SQL_GET_PURCHASE_LOGS = 
        "SELECT logid, logtext, logdate, adminid " +
        "FROM public.purchaselog " +
        "ORDER BY logdate DESC";

    @PersistenceContext
    private EntityManager entityManager;

    // 获取借阅次数排行前10的图书
    public List<Object[]> getTop10ByBorrowCount() {
        try {
            return entityManager.createNativeQuery(SQL_TOP_10_BY_BORROW_COUNT).getResultList();
        } catch (Exception e) {
            logger.error("获取借阅次数排行失败", e);
            return List.of();
        }
    }

    // 获取借阅时长排行前10的图书
    public List<Object[]> getTop10ByBorrowDuration() {
        try {
            return entityManager.createNativeQuery(SQL_TOP_10_BY_BORROW_DURATION).getResultList();
        } catch (Exception e) {
            logger.error("获取借阅时长排行失败", e);
            return List.of();
        }
    }

    // 获取图书实例借阅排行前10
    public List<Object[]> getTop10ByInstanceBorrow() {
        try {
            return entityManager.createNativeQuery(SQL_TOP_10_BY_INSTANCE_BORROW).getResultList();
        } catch (Exception e) {
            logger.error("获取图书实例借阅排行失败", e);
            return List.of();
        }
    }

    // 获取所有采购日志
    public List<Purchaselog> getPurchaseLogs() {
        try {
            logger.info("开始查询采购日志");
            List<Object[]> results = entityManager.createNativeQuery(SQL_GET_PURCHASE_LOGS).getResultList();
            logger.info("查询到 " + results.size() + " 条采购日志记录");
            
            // 将 Object[] 转换为 Purchaselog 实体
            return results.stream().map(row -> {
                Purchaselog log = new Purchaselog();
                // row[0] = logid, row[1] = logtext, row[2] = logdate, row[3] = adminid
                
                // 调试信息
                logger.infof("处理日志记录: logid=%s, logtext=%s, logdate=%s, adminid=%s", 
                    row[0], row[1], row[2], row[3]);
                
                if (row[0] != null) {
                    log.setLogId(((Number) row[0]).longValue());
                }
                if (row[1] != null) {
                    log.setLogText(row[1].toString());
                }
                
                // 处理日期字段
                if (row[2] != null) {
                    try {
                        if (row[2] instanceof java.sql.Timestamp) {
                            log.setLogDate(((java.sql.Timestamp) row[2]).toLocalDateTime());
                            logger.infof("日期转换成功 (Timestamp): %s", log.getLogDate());
                        } else if (row[2] instanceof java.sql.Date) {
                            log.setLogDate(((java.sql.Date) row[2]).toLocalDate().atStartOfDay());
                            logger.infof("日期转换成功 (Date): %s", log.getLogDate());
                        } else if (row[2] instanceof LocalDateTime) {
                            log.setLogDate((LocalDateTime) row[2]);
                            logger.infof("日期转换成功 (LocalDateTime): %s", log.getLogDate());
                        } else {
                            logger.warn("未知的日期类型: " + row[2].getClass().getName());
                        }
                    } catch (Exception e) {
                        logger.error("日期转换失败: " + e.getMessage(), e);
                    }
                } else {
                    logger.warn("logdate 字段为 null");
                }
                
                // 处理管理员ID字段
                if (row[3] != null) {
                    try {
                        if (row[3] instanceof Number) {
                            log.setAdminId(((Number) row[3]).intValue());
                            logger.infof("adminId 转换成功 (Number): %d", log.getAdminId());
                        } else if (row[3] instanceof String) {
                            String adminIdStr = row[3].toString().trim();
                            if (!adminIdStr.isEmpty()) {
                                log.setAdminId(Integer.parseInt(adminIdStr));
                                logger.infof("adminId 转换成功 (String): %d", log.getAdminId());
                            } else {
                                logger.warn("adminId 字符串为空");
                            }
                        } else {
                            logger.warn("未知的 adminId 类型: " + row[3].getClass().getName());
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("无法解析 adminId: " + row[3] + ", 错误: " + e.getMessage());
                    }
                } else {
                    logger.warn("adminid 字段为 null");
                }
                
                logger.infof("转换后的日志: logId=%d, logText=%s, logDate=%s, adminId=%d", 
                    log.getLogId(), log.getLogText(), log.getLogDate(), log.getAdminId());
                
                return log;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取采购日志失败", e);
            e.printStackTrace();
            return List.of();
        }
    }

    // 添加采购日志
    public void addPurchaseLog(String logText, Integer adminId) {
        // 参数验证
        if (adminId == null) {
            logger.warn("尝试添加采购日志时管理员ID为null");
            throw new IllegalArgumentException("管理员ID不能为空");
        }
        if (logText == null || logText.trim().isEmpty()) {
            logger.warn("尝试添加空的采购日志文本");
            throw new IllegalArgumentException("采购日志文本不能为空");
        }
        
        try {
            Purchaselog log = new Purchaselog();
            log.setLogText(logText);
            log.setAdminId(adminId);
            log.setLogDate(LocalDateTime.now());
            entityManager.persist(log);
            logger.infof("添加采购日志成功: 管理员ID=%d, 日志内容=%s", adminId, logText);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("添加采购日志失败", e);
            throw new RuntimeException("添加采购日志失败: " + e.getMessage(), e);
        }
    }
}