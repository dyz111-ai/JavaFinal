package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.ReportDetailDto;
import com.example.demo0.admin.dto.ReportDto;
import com.example.demo0.admin.dto.HandleReportDto;
import com.example.demo0.admin.entity.Comment_Table;
import com.example.demo0.admin.entity.Bookinfo;
import com.example.demo0.admin.entity.Reader;
import com.example.demo0.admin.entity.Report;
import com.example.demo0.admin.repository.ReportRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
@Transactional
public class ReportService {

    private static final Logger LOGGER = Logger.getLogger(ReportService.class);

    // 举报状态常量 - 与Repository保持一致
    public static final String REPORT_STATUS_PENDING = "待处理";
    public static final String REPORT_STATUS_COMPLETED = "处理完成";
    public static final String REPORT_STATUS_REJECTED = "驳回";

    // 评论状态常量
    public static final String COMMENT_STATUS_NORMAL = "正常";
    public static final String COMMENT_STATUS_DELETED = "已删除";

    // 用户状态常量
    public static final String USER_STATUS_NORMAL = "正常";
    public static final String USER_STATUS_FROZEN = "冻结";

    // 处理动作常量
    public static final String ACTION_APPROVE = "approve";
    public static final String ACTION_REJECT = "reject";

    @Inject
    private ReportRepository reportRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 获取所有待处理的举报详情
     */
    public List<ReportDetailDto> getPendingReports() {
        try {
            LOGGER.info("获取待处理举报列表");
            List<Report> reports = reportRepository.getPendingReports();

            return reports.stream()
                    .map(this::convertToReportDetailDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("获取待处理举报列表失败", e);
            throw new RuntimeException("获取待处理举报列表失败", e);
        }
    }

    /**
     * 获取所有举报（包括已处理的）
     */
    public List<ReportDetailDto> getAllReports() {
        try {
            LOGGER.info("获取所有举报列表");
            String jpql = "SELECT r FROM Report r ORDER BY r.reportTime DESC";
            List<Report> reports = entityManager.createQuery(jpql, Report.class).getResultList();

            return reports.stream()
                    .map(this::convertToReportDetailDto)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("获取所有举报列表失败", e);
            throw new RuntimeException("获取所有举报列表失败", e);
        }
    }

    /**
     * 将Report实体转换为ReportDetailDto
     */
    private ReportDetailDto convertToReportDetailDto(Report report) {
        if (report == null) {
            return null;
        }

        ReportDetailDto dto = new ReportDetailDto();

        // 设置举报基本信息
        dto.setReportId(report.getReportId());
        dto.setReportReason(report.getReportReason());
        dto.setReportTime(report.getReportTime());
        dto.setReportStatus(report.getStatus());
        dto.setCommentId(report.getCommentId());
        dto.setReporterId(report.getReaderId());

        // 设置处理人ID（如果有）
        dto.setLibrarianId(report.getLibrarianId());

        // 获取评论详情
        if (report.getCommentId() != null) {
            try {
                Comment_Table comment = entityManager.find(Comment_Table.class, report.getCommentId());
                if (comment != null) {
                    dto.setCommentId(comment.getCommentId());
                    dto.setReviewContent(comment.getReviewContent());
                    dto.setCommentTime(comment.getCreateTime());
                    dto.setIsbn(comment.getIsbn());
                    dto.setCommenterId(comment.getReaderId());

                    // 获取评论者信息
                    Reader commenter = entityManager.find(Reader.class, comment.getReaderId());
                    if (commenter != null) {
                        dto.setCommenterNickname(commenter.getNickname());
                        dto.setCommenterAccountStatus(commenter.getAccountStatus());
                    }

                    // 获取图书信息
                    Bookinfo bookinfo = entityManager.find(Bookinfo.class, comment.getIsbn());
                    if (bookinfo != null) {
                        dto.setBookTitle(bookinfo.getTitle());
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("获取评论详情失败，评论ID: " + report.getCommentId(), e);
            }
        }

        // 获取举报人信息
        if (report.getReaderId() != null) {
            try {
                Reader reporter = entityManager.find(Reader.class, report.getReaderId());
                if (reporter != null) {
                    dto.setReporterNickname(reporter.getNickname());
                }
            } catch (Exception e) {
                LOGGER.warn("获取举报人信息失败，读者ID: " + report.getReaderId(), e);
            }
        }

        return dto;
    }

    /**
     * 处理举报
     */
    public boolean handleReport(HandleReportDto dto) {
        try {
            // 验证必要参数
            if (dto == null) {
                throw new IllegalArgumentException("举报数据不能为空");
            }

            Integer reportId = dto.getReportId();
            String action = dto.getAction();
            Integer librarianId = dto.getLibrarianId();

            if (reportId == null) {
                throw new IllegalArgumentException("举报ID不能为空");
            }

            if (action == null || action.trim().isEmpty()) {
                throw new IllegalArgumentException("处理动作不能为空");
            }

            if (librarianId == null) {
                throw new IllegalArgumentException("管理员ID不能为空");
            }

            LOGGER.info("处理举报，举报ID: " + reportId + ", 动作: " + action + ", 管理员ID: " + librarianId);

            // 获取举报记录
            Report report = reportRepository.findReportById(reportId);
            if (report == null) {
                LOGGER.warn("举报记录不存在，ID: " + reportId);
                throw new IllegalArgumentException("举报记录不存在");
            }

            // 检查举报状态，只有待处理的举报可以处理
            if (!REPORT_STATUS_PENDING.equals(report.getStatus())) {
                LOGGER.warn("举报状态不是待处理，无法处理，当前状态: " + report.getStatus());
                throw new IllegalArgumentException("该举报已被处理");
            }

            // 根据action设置相应的状态
            switch (action.toLowerCase()) {
                case ACTION_APPROVE:
                    return handleApproveAction(dto, report, librarianId);
                case ACTION_REJECT:
                    return handleRejectAction(dto, report, librarianId);
                default:
                    throw new IllegalArgumentException("无效的处理动作: " + action);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("处理举报参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("处理举报失败", e);
            throw new RuntimeException("处理举报失败", e);
        }
    }

    /**
     * 处理批准举报的逻辑
     */
    private boolean handleApproveAction(HandleReportDto dto, Report report, Integer librarianId) {
        LOGGER.info("批准举报，举报ID: " + report.getReportId());

        try {
            // 设置DTO中的必要信息
            dto.setNewReportStatus(REPORT_STATUS_COMPLETED);
            dto.setNewCommentStatus(COMMENT_STATUS_DELETED);
            dto.setLibrarianId(librarianId);
            dto.setCommentId(report.getCommentId());

            // 获取评论者ID
            if (report.getCommentId() != null) {
                Comment_Table comment = entityManager.find(Comment_Table.class, report.getCommentId());
                if (comment != null) {
                    dto.setCommenterId(comment.getReaderId());

                    // 如果需要禁言用户，设置标志
                    if (dto.isBanUser() && comment.getReaderId() != null) {
                        dto.setCommenterId(comment.getReaderId());
                    }
                }
            }

            // 调用Repository处理举报
            boolean result = reportRepository.handleReport(dto);

            if (result) {
                LOGGER.info("举报处理成功，举报ID: " + report.getReportId() +
                        ", 评论ID: " + report.getCommentId());
            } else {
                LOGGER.warn("举报处理失败，举报ID: " + report.getReportId());
            }

            return result;
        } catch (Exception e) {
            LOGGER.error("批准举报处理失败，举报ID: " + report.getReportId(), e);
            throw new RuntimeException("批准举报处理失败", e);
        }
    }

    /**
     * 处理驳回举报的逻辑
     */
    private boolean handleRejectAction(HandleReportDto dto, Report report, Integer librarianId) {
        LOGGER.info("驳回举报，举报ID: " + report.getReportId());

        try {
            // 设置DTO中的必要信息
            dto.setNewReportStatus(REPORT_STATUS_REJECTED);
            dto.setBanUser(false);
            dto.setLibrarianId(librarianId);
            dto.setCommentId(report.getCommentId());

            // 对于驳回操作，不修改评论状态，也不禁言用户
            dto.setNewCommentStatus(null);
            dto.setCommenterId(null);

            // 调用Repository处理举报
            boolean result = reportRepository.handleReport(dto);

            if (result) {
                LOGGER.info("举报驳回成功，举报ID: " + report.getReportId());
            } else {
                LOGGER.warn("举报驳回失败，举报ID: " + report.getReportId());
            }

            return result;
        } catch (Exception e) {
            LOGGER.error("驳回举报处理失败，举报ID: " + report.getReportId(), e);
            throw new RuntimeException("驳回举报处理失败", e);
        }
    }

    /**
     * 添加新举报
     */
    public void addReport(ReportDto reportDto) {
        try {
            // 参数验证
            if (reportDto == null) {
                throw new IllegalArgumentException("举报数据不能为空");
            }

            if (reportDto.getCommentId() == null) {
                throw new IllegalArgumentException("评论ID不能为空");
            }

            if (reportDto.getReaderId() == null) {
                throw new IllegalArgumentException("举报人ID不能为空");
            }

            if (reportDto.getReportReason() == null || reportDto.getReportReason().trim().isEmpty()) {
                throw new IllegalArgumentException("举报原因不能为空");
            }

            // 检查评论是否存在
            Comment_Table comment = entityManager.find(Comment_Table.class, reportDto.getCommentId());
            if (comment == null) {
                throw new IllegalArgumentException("评论不存在");
            }

            // 检查评论状态，只有正常状态的评论可以被举报
            if (!COMMENT_STATUS_NORMAL.equals(comment.getStatus())) {
                throw new IllegalArgumentException("该评论状态不可举报");
            }

            // 检查举报人是否存在
            Reader reporter = entityManager.find(Reader.class, reportDto.getReaderId());
            if (reporter == null) {
                throw new IllegalArgumentException("举报人不存在");
            }

            LOGGER.info("添加新举报，评论ID: " + reportDto.getCommentId() +
                    ", 举报人ID: " + reportDto.getReaderId());

            // 设置举报时间
            reportDto.setReportTime(LocalDateTime.now());

            reportRepository.addReport(reportDto);
            LOGGER.info("举报添加成功，评论ID: " + reportDto.getCommentId());
        } catch (IllegalArgumentException e) {
            LOGGER.error("添加举报参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("添加举报失败", e);
            throw new RuntimeException("添加举报失败", e);
        }
    }

    /**
     * 根据ID获取举报详情
     */
    public ReportDetailDto getReportById(Integer reportId) {
        try {
            if (reportId == null) {
                throw new IllegalArgumentException("举报ID不能为空");
            }

            LOGGER.debug("获取举报详情，ID: " + reportId);
            Report report = reportRepository.findReportById(reportId);

            if (report == null) {
                LOGGER.warn("举报记录不存在，ID: " + reportId);
                return null;
            }

            return convertToReportDetailDto(report);
        } catch (Exception e) {
            LOGGER.error("获取举报详情失败，ID: " + reportId, e);
            throw new RuntimeException("获取举报详情失败", e);
        }
    }

    /**
     * 检查用户是否可以举报某评论
     */
    public boolean canReportComment(Integer readerId, Integer commentId) {
        try {
            if (readerId == null || commentId == null) {
                return false;
            }

            // 检查是否已经举报过该评论
            String jpql = "SELECT COUNT(r) FROM Report r WHERE r.readerId = :readerId AND r.commentId = :commentId";
            Long count = entityManager.createQuery(jpql, Long.class)
                    .setParameter("readerId", readerId)
                    .setParameter("commentId", commentId)
                    .getSingleResult();

            return count == 0;
        } catch (Exception e) {
            LOGGER.error("检查举报权限失败", e);
            return false;
        }
    }
}