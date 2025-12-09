package com.example.demo0.book.service;

import com.example.demo0.book.model.CommentRecord;
import com.example.demo0.book.repository.CommentRepository;

import java.util.List;

public class CommentService {

    private final CommentRepository repository = new CommentRepository();

    /** 查询某本书的评论（按时间倒序，limit 上限 500） */
    public List<CommentRecord> findByIsbn(String isbn, int limit) {
        String norm = normalizeIsbn(isbn);
        int lim = Math.max(1, Math.min(500, limit));
        return repository.findByIsbn(norm, lim);
    }

    /** 按 ID 查询（预留） */
    public List<CommentRecord> findById(long id) {
        return repository.findById(id);
    }

    /** 新增评论，readerId 按要求可传固定值 1 */
    public int addComment(long readerId, String isbn, Short rating, String content) {
        String norm = normalizeIsbn(isbn);
        // 允许空内容时直接返回 0（也可抛异常）
        if (norm == null || norm.isBlank() || content == null || content.isBlank()) {
            return 0;
        }
        Short r = clampRating(rating);
        return repository.addComment(readerId, norm, r, content, "正常");
    }

    private Short clampRating(Short r) {
        if (r == null) return 5;
        int x = Math.max(1, Math.min(5, r));
        return (short) x;
    }

    /** 规范化 ISBN：去空格，移除除数字和 X 以外字符，保持大小写不敏感 */
    private String normalizeIsbn(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return t;
        return t.replaceAll("[^0-9Xx]", "");
    }
}





























