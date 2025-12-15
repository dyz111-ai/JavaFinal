package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Report;
import com.example.demo0.admin.entity.Comment_Table;
import com.example.demo0.admin.entity.Reader;
import com.example.demo0.admin.entity.Bookinfo;
import com.example.demo0.admin.dto.ReportDto;
import com.example.demo0.admin.dto.HandleReportDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
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
            // PostgreSQL: 使用 public schema，字段名小写
            String nativeSql = "SELECT reportid, commentid, readerid, reportreason, reporttime, status, librarianid " +
                              "FROM public.report WHERE status = ? ORDER BY reporttime ASC";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(nativeSql)
                    .setParameter(1, REPORT_STATUS_PENDING)
                    .getResultList();
            
            logger.infof("使用原生SQL查询，找到 %d 条待处理举报 (状态值: [%s])", results.size(), REPORT_STATUS_PENDING);
            
            // 如果没找到，尝试查询所有记录看看实际状态值
            if (results.isEmpty()) {
                String allSql = "SELECT reportid, status FROM public.report ORDER BY reporttime ASC";
                @SuppressWarnings("unchecked")
                List<Object[]> allResults = entityManager.createNativeQuery(allSql).getResultList();
                
                logger.warnf("未找到待处理举报，数据库中所有记录的状态值:");
                for (Object[] row : allResults) {
                    String statusStr = row[1] != null ? row[1].toString() : "NULL";
                    logger.warnf("  举报ID: %s, 状态: [%s]", row[0], statusStr);
                }
                
                // 尝试查询所有不同的状态值
                String distinctSql = "SELECT DISTINCT status FROM public.report";
                @SuppressWarnings("unchecked")
                List<String> distinctStatuses = entityManager.createNativeQuery(distinctSql).getResultList();
                logger.warnf("数据库中实际的状态值列表: %s", distinctStatuses);
            }
            
            // 转换为Report实体
            List<Report> reports = new java.util.ArrayList<>();
            for (Object[] row : results) {
                Report report = new Report();
                report.setReportId(((Number) row[0]).intValue());
                report.setCommentId(((Number) row[1]).intValue());
                
                // PostgreSQL中readerid可能是VARCHAR，需要转换
                Object readerIdObj = row[2];
                if (readerIdObj instanceof String) {
                    report.setReaderId(Integer.parseInt((String) readerIdObj));
                } else {
                    report.setReaderId(((Number) readerIdObj).intValue());
                }
                
                report.setReportReason(row[3] != null ? row[3].toString() : null);
                
                if (row[4] != null) {
                    if (row[4] instanceof java.sql.Timestamp) {
                        report.setReportTime(((java.sql.Timestamp) row[4]).toLocalDateTime());
                    } else if (row[4] instanceof LocalDateTime) {
                        report.setReportTime((LocalDateTime) row[4]);
                    } else if (row[4] instanceof java.time.OffsetDateTime) {
                        report.setReportTime(((java.time.OffsetDateTime) row[4]).toLocalDateTime());
                    }
                }
                
                report.setStatus(row[5] != null ? row[5].toString() : null);
                
                if (row[6] != null) {
                    Object libIdObj = row[6];
                    if (libIdObj instanceof String) {
                        report.setLibrarianId(Integer.parseInt((String) libIdObj));
                    } else {
                        report.setLibrarianId(((Number) libIdObj).intValue());
                    }
                }
                
                reports.add(report);
            }
            
            return reports;
        } catch (Exception e) {
            logger.error("获取待处理举报失败", e);
            
            // 如果原生SQL失败，回退到JPQL
        try {
            String jpql = "SELECT r FROM Report r WHERE r.status = :status ORDER BY r.reportTime ASC";
                List<Report> jpqlResults = entityManager.createQuery(jpql, Report.class)
                    .setParameter("status", REPORT_STATUS_PENDING)
                    .getResultList();
                return jpqlResults;
            } catch (Exception e2) {
                logger.error("JPQL查询也失败", e2);
            return List.of();
            }
        }
    }

    @Transactional
    public boolean handleReport(HandleReportDto dto) {
        // 参数验证
        if (dto == null || dto.getReportId() == null) {
            logger.warn("处理举报参数无效: dto为空或reportId为null");
            System.out.println("[ReportRepository] ❌ 参数验证失败: dto=" + dto + ", reportId=" + (dto != null ? dto.getReportId() : "null"));
            return false;
        }

        System.out.println("[ReportRepository] ========== 开始处理举报 ==========");
        System.out.println("[ReportRepository] ReportID: " + dto.getReportId());
        System.out.println("[ReportRepository] NewReportStatus: " + dto.getNewReportStatus());
        System.out.println("[ReportRepository] LibrarianID: " + dto.getLibrarianId());
        System.out.println("[ReportRepository] CommentID: " + dto.getCommentId());
        System.out.println("[ReportRepository] NewCommentStatus: " + dto.getNewCommentStatus());
        System.out.println("[ReportRepository] CommenterID: " + dto.getCommenterId());
        System.out.println("[ReportRepository] BanUser: " + dto.isBanUser());

        try {
            // 1. 更新举报状态 - 使用原生SQL
            String updateReportSql = "UPDATE public.report SET status = ?, librarianid = ? WHERE reportid = ?";
            System.out.println("[ReportRepository] 执行SQL: " + updateReportSql);
            System.out.println("[ReportRepository] 参数: status=" + dto.getNewReportStatus() + ", librarianid=" + dto.getLibrarianId() + ", reportid=" + dto.getReportId());
            
            int reportUpdated = entityManager.createNativeQuery(updateReportSql)
                    .setParameter(1, dto.getNewReportStatus())
                    .setParameter(2, dto.getLibrarianId())
                    .setParameter(3, dto.getReportId())
                    .executeUpdate();
            
            System.out.println("[ReportRepository] 更新举报记录数: " + reportUpdated);
            
            if (reportUpdated == 0) {
            logger.warn("未找到举报记录: " + dto.getReportId());
                System.out.println("[ReportRepository] ❌ 未找到举报记录: " + dto.getReportId());
            return false;
        }

            // 2. 更新评论状态 - 使用原生SQL
            if (dto.getNewCommentStatus() != null && dto.getCommentId() != null) {
                String updateCommentSql = "UPDATE public.comment_table SET status = ? WHERE commentid = ?";
                System.out.println("[ReportRepository] 执行SQL: " + updateCommentSql);
                System.out.println("[ReportRepository] 参数: status=" + dto.getNewCommentStatus() + ", commentid=" + dto.getCommentId());
                
                int commentUpdated = entityManager.createNativeQuery(updateCommentSql)
                        .setParameter(1, dto.getNewCommentStatus())
                        .setParameter(2, dto.getCommentId())
                        .executeUpdate();
                
                System.out.println("[ReportRepository] 更新评论记录数: " + commentUpdated);
                
                if (commentUpdated == 0) {
                    logger.warn("未找到评论记录: " + dto.getCommentId());
                    System.out.println("[ReportRepository] ⚠️ 未找到评论记录: " + dto.getCommentId());
                }
            } else {
                System.out.println("[ReportRepository] 跳过更新评论状态: NewCommentStatus=" + dto.getNewCommentStatus() + ", CommentID=" + dto.getCommentId());
            }

            // 3. 禁言用户 - 使用原生SQL
            if (dto.isBanUser() && dto.getCommenterId() != null) {
                String updateReaderSql = "UPDATE public.reader SET accountstatus = ? WHERE readerid = ?";
                System.out.println("[ReportRepository] 执行SQL: " + updateReaderSql);
                System.out.println("[ReportRepository] 参数: accountstatus=" + USER_STATUS_FROZEN + ", readerid=" + dto.getCommenterId());
                
                int readerUpdated = entityManager.createNativeQuery(updateReaderSql)
                        .setParameter(1, USER_STATUS_FROZEN)
                        .setParameter(2, dto.getCommenterId())
                        .executeUpdate();
                
                System.out.println("[ReportRepository] 更新读者记录数: " + readerUpdated);
                
                if (readerUpdated == 0) {
                    logger.warn("未找到用户记录: " + dto.getCommenterId());
                    System.out.println("[ReportRepository] ⚠️ 未找到用户记录: " + dto.getCommenterId());
                }
            } else {
                System.out.println("[ReportRepository] 跳过禁言用户: BanUser=" + dto.isBanUser() + ", CommenterID=" + dto.getCommenterId());
            }
            
            // 刷新EntityManager以确保更改被提交
            entityManager.flush();
            System.out.println("[ReportRepository] ✅ 处理举报成功");
            System.out.println("[ReportRepository] ========== 处理举报完成 ==========");
            return true;
        } catch (Exception e) {
            logger.error("处理举报失败: " + dto.getReportId(), e);
            System.out.println("[ReportRepository] ❌ 处理举报失败: " + e.getMessage());
            e.printStackTrace();
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
        
        // 使用原生SQL插入举报记录
        LocalDateTime now = LocalDateTime.now();
        String insertSql = "INSERT INTO public.report (commentid, readerid, reportreason, reporttime, status, librarianid) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";
        
        entityManager.createNativeQuery(insertSql)
                .setParameter(1, reportDto.getCommentId())
                .setParameter(2, reportDto.getReaderId())
                .setParameter(3, reportDto.getReportReason())
                .setParameter(4, java.sql.Timestamp.valueOf(now))
                .setParameter(5, REPORT_STATUS_PENDING)
                .setParameter(6, null) // 新创建的举报没有处理人
                .executeUpdate();
        
        logger.infof("新增举报记录: commentId=%s", reportDto.getCommentId());
    }

    public Report findReportById(Integer reportId) {
        if (reportId == null) {
            return null;
        }
        try {
            // 使用原生SQL查询举报记录
            String sql = "SELECT reportid, commentid, readerid, reportreason, reporttime, status, librarianid " +
                        "FROM public.report WHERE reportid = ?";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, reportId)
                    .getResultList();
            
            if (results.isEmpty()) {
                return null;
            }
            
            Object[] row = results.get(0);
            Report report = new Report();
            report.setReportId(((Number) row[0]).intValue());
            report.setCommentId(((Number) row[1]).intValue());
            
            // PostgreSQL中readerid可能是VARCHAR，需要转换
            Object readerIdObj = row[2];
            if (readerIdObj instanceof String) {
                report.setReaderId(Integer.parseInt((String) readerIdObj));
            } else {
                report.setReaderId(((Number) readerIdObj).intValue());
            }
            
            report.setReportReason(row[3] != null ? row[3].toString() : null);
            
            if (row[4] != null) {
                if (row[4] instanceof java.sql.Timestamp) {
                    report.setReportTime(((java.sql.Timestamp) row[4]).toLocalDateTime());
                } else if (row[4] instanceof LocalDateTime) {
                    report.setReportTime((LocalDateTime) row[4]);
                } else if (row[4] instanceof java.time.OffsetDateTime) {
                    report.setReportTime(((java.time.OffsetDateTime) row[4]).toLocalDateTime());
                }
            }
            
            report.setStatus(row[5] != null ? row[5].toString() : null);
            
            if (row[6] != null) {
                Object libIdObj = row[6];
                if (libIdObj instanceof String) {
                    report.setLibrarianId(Integer.parseInt((String) libIdObj));
                } else {
                    report.setLibrarianId(((Number) libIdObj).intValue());
                }
            }
            
            return report;
        } catch (Exception e) {
            logger.error("查找举报记录失败: " + reportId, e);
            return null;
        }
    }

    // 使用原生SQL查询评论详情
    public Comment_Table findCommentById(Integer commentId) {
        if (commentId == null) {
            return null;
        }
        try {
            // 使用小写表名
            String sql = "SELECT commentid, readerid, isbn, rating, reviewcontent, createtime, status " +
                        "FROM public.comment_table WHERE commentid = ?";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, commentId)
                    .getResultList();
            
            if (results.isEmpty()) {
                return null;
            }
            
            Object[] row = results.get(0);
            Comment_Table comment = new Comment_Table();
            comment.setCommentId(((Number) row[0]).intValue());
            
            Object readerIdObj = row[1];
            if (readerIdObj instanceof String) {
                comment.setReaderId(Integer.parseInt((String) readerIdObj));
            } else {
                comment.setReaderId(((Number) readerIdObj).intValue());
            }
            
            comment.setIsbn(row[2] != null ? row[2].toString() : null);
            comment.setRating(row[3] != null ? ((Number) row[3]).intValue() : null);
            comment.setReviewContent(row[4] != null ? row[4].toString() : null);
            
            if (row[5] != null) {
                if (row[5] instanceof java.sql.Timestamp) {
                    comment.setCreateTime(((java.sql.Timestamp) row[5]).toLocalDateTime());
                } else if (row[5] instanceof LocalDateTime) {
                    comment.setCreateTime((LocalDateTime) row[5]);
                }
            }
            
            comment.setStatus(row[6] != null ? row[6].toString() : null);
            
            return comment;
        } catch (Exception e) {
            logger.error("查询评论失败: " + commentId, e);
            return null;
        }
    }

    // 使用原生SQL查询读者信息
    public Reader findReaderById(Integer readerId) {
        if (readerId == null) {
            return null;
        }
        try {
            String sql = "SELECT readerid, username, password, fullname, nickname, avatar, creditscore, accountstatus, permission " +
                        "FROM public.reader WHERE readerid = ?";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, readerId)
                    .getResultList();
            
            if (results.isEmpty()) {
                return null;
            }
            
            Object[] row = results.get(0);
            Reader reader = new Reader();
            
            Object idObj = row[0];
            if (idObj instanceof String) {
                reader.setReaderId(Integer.parseInt((String) idObj));
            } else {
                reader.setReaderId(((Number) idObj).intValue());
            }
            
            reader.setUsername(row[1] != null ? row[1].toString() : null);
            reader.setPassword(row[2] != null ? row[2].toString() : null);
            reader.setFullname(row[3] != null ? row[3].toString() : null);
            reader.setNickname(row[4] != null ? row[4].toString() : null);
            reader.setAvatar(row[5] != null ? row[5].toString() : null);
            reader.setCreditScore(row[6] != null ? ((Number) row[6]).intValue() : null);
            reader.setAccountStatus(row[7] != null ? row[7].toString() : null);
            reader.setPermission(row[8] != null ? row[8].toString() : null);
            
            return reader;
        } catch (Exception e) {
            logger.error("查询读者失败: " + readerId, e);
            return null;
        }
    }

    // 使用原生SQL查询图书信息
    public Bookinfo findBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return null;
        }
        try {
            String sql = "SELECT isbn, title, author, stock " +
                        "FROM public.bookinfo WHERE isbn = ?";
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, isbn)
                    .getResultList();
            
            if (results.isEmpty()) {
                return null;
            }
            
            Object[] row = results.get(0);
            Bookinfo bookinfo = new Bookinfo();
            bookinfo.setIsbn(row[0] != null ? row[0].toString() : null);
            bookinfo.setTitle(row[1] != null ? row[1].toString() : null);
            bookinfo.setAuthor(row[2] != null ? row[2].toString() : null);
            bookinfo.setStock(row[3] != null ? ((Number) row[3]).intValue() : null);
            
            return bookinfo;
        } catch (Exception e) {
            logger.error("查询图书失败: " + isbn, e);
            return null;
        }
    }

    // 使用原生SQL检查是否已举报过该评论
    public boolean hasReportedComment(Integer readerId, Integer commentId) {
        if (readerId == null || commentId == null) {
            return false;
        }
        try {
            String sql = "SELECT COUNT(*) FROM public.report WHERE readerid = ? AND commentid = ?";
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, readerId)
                    .setParameter(2, commentId)
                    .getResultList();
            
            if (results.isEmpty()) {
                return false;
            }
            
            Object countObj = results.get(0);
            long count = 0;
            if (countObj instanceof Number) {
                count = ((Number) countObj).longValue();
            } else {
                count = Long.parseLong(countObj.toString());
            }
            
            return count > 0;
        } catch (Exception e) {
            logger.error("检查举报记录失败", e);
            return false;
        }
    }
}