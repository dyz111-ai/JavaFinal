package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Report;
import com.example.demo0.admin.entity.Comment_Table;
import com.example.demo0.admin.entity.Reader;
import com.example.demo0.admin.dto.ReportDto;
import com.example.demo0.admin.dto.HandleReportDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReportRepository {
    // 添加日志记录器
    private static final Logger logger = Logger.getLogger(ReportRepository.class.getName());
    
    // 举报状态常量定义
    public static final String REPORT_STATUS_PENDING = "待处理";
    public static final String REPORT_STATUS_REJECTED = "驳回";
    public static final String REPORT_STATUS_COMPLETED = "处理完成";
    
    // 用户状态常量
    public static final String USER_STATUS_FROZEN = "冻结";

    @PersistenceContext
    private EntityManager entityManager;

    // 获取待处理的举报
    public List<Report> getPendingReports() {
        try {
            String jpql = "SELECT r FROM Report r WHERE r.status = :status ORDER BY r.reportTime ASC";
            return entityManager.createQuery(jpql, Report.class)
                    .setParameter("status", REPORT_STATUS_PENDING)
                    .getResultList();
        } catch (Exception e) {
            logger.error("获取待处理举报失败", e);
            return List.of();
        }
    }

    public boolean handleReport(HandleReportDto dto) {
        // 参数验证
        if (dto == null || dto.getReportId() == null) {
            logger.warn("处理举报参数无效: dto为空或reportId为null");
            return false;
        }

        Report report = entityManager.find(Report.class, dto.getReportId());
        if (report == null) {
            logger.warn("未找到举报记录: " + dto.getReportId());
            return false;
        }

        try {
            // 1. 更新举报状态
            report.setStatus(dto.getNewReportStatus());
            report.setLibrarianId(dto.getLibrarianId());
            entityManager.merge(report);

            // 2. 更新评论状态
            if (dto.getNewCommentStatus() != null && dto.getCommentId() != null) {
                Comment_Table comment = entityManager.find(Comment_Table.class, dto.getCommentId());
                if (comment != null) {
                    comment.setStatus(dto.getNewCommentStatus());
                    entityManager.merge(comment);
                } else {
                    logger.warn("未找到评论记录: " + dto.getCommentId());
                }
            }

            // 3. 禁言用户
            if (dto.isBanUser() && dto.getCommenterId() != null) {
                Reader reader = entityManager.find(Reader.class, dto.getCommenterId());
                if (reader != null) {
                    reader.setAccountStatus(USER_STATUS_FROZEN);
                    entityManager.merge(reader);
                } else {
                    logger.warn("未找到用户记录: " + dto.getCommenterId());
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("处理举报失败: " + dto.getReportId(), e);
            return false;
        }
    }

    public void addReport(ReportDto reportDto) {
        // 参数验证
        if (reportDto == null) {
            throw new IllegalArgumentException("ReportDto cannot be null");
        }
        if (reportDto.getCommentId() == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        if (reportDto.getReaderId() == null) {
            throw new IllegalArgumentException("Reader ID cannot be null");
        }
        if (reportDto.getReportReason() == null || reportDto.getReportReason().trim().isEmpty()) {
            throw new IllegalArgumentException("Report reason cannot be empty");
        }
        
        Report report = new Report();
        report.setCommentId(reportDto.getCommentId());
        report.setReaderId(reportDto.getReaderId());
        report.setReportReason(reportDto.getReportReason());
        report.setReportTime(LocalDateTime.now());
        // 使用常量而不是硬编码
        report.setStatus(REPORT_STATUS_PENDING);
        // 新创建的举报没有处理人，应设为null
        report.setLibrarianId(null);

        // 保存到数据库
        entityManager.persist(report);
        logger.infof("新增举报记录: reportId=%s, commentId=%s", report.getReportId(), report.getCommentId());
    }

    public Report findReportById(Integer reportId) {
        try {
            return entityManager.find(Report.class, reportId);
        } catch (Exception e) {
            logger.error("查找举报记录失败: " + reportId, e);
            return null;
        }
    }
}