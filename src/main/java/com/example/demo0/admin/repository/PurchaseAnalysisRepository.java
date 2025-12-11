package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Purchaselog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PurchaseAnalysisRepository {
    // 添加日志记录器
    private static final Logger logger = Logger.getLogger(PurchaseAnalysisRepository.class.getName());
    
    // 视图名称常量定义
    public static final String VIEW_BOOK_RANK_BY_BORROW_COUNT = "V_BookRank_By_BorrowCount";
    public static final String VIEW_BOOK_RANK_BY_BORROW_DURATION = "V_BookRank_By_BorrowDuration";
    public static final String VIEW_BOOK_RANK_BY_INSTANCE_BORROW = "V_BookRank_By_InstanceBorrow";
    
    // SQL查询常量
    private static final String SQL_TOP_10_BY_BORROW_COUNT = "SELECT isbn, title, author, metric_value FROM " + VIEW_BOOK_RANK_BY_BORROW_COUNT + " LIMIT 10";
    private static final String SQL_TOP_10_BY_BORROW_DURATION = "SELECT isbn, title, author, metric_value FROM " + VIEW_BOOK_RANK_BY_BORROW_DURATION + " LIMIT 10";
    private static final String SQL_TOP_10_BY_INSTANCE_BORROW = "SELECT barcode as isbn, title, author, metric_value FROM " + VIEW_BOOK_RANK_BY_INSTANCE_BORROW + " LIMIT 10";
    private static final String JPQL_GET_PURCHASE_LOGS = "SELECT p FROM Purchaselog p ORDER BY p.logDate DESC";

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
            return entityManager.createQuery(JPQL_GET_PURCHASE_LOGS, Purchaselog.class).getResultList();
        } catch (Exception e) {
            logger.error("获取采购日志失败", e);
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