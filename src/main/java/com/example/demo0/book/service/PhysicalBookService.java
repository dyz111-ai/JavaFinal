package com.example.demo0.book.service;

import com.example.demo0.book.model.PhysicalBook;
import com.example.demo0.book.repository.BookRepository;

import java.util.List;

/**
 * 实体书服务层
 * 对应原项目的 BookShelfService.SearchBookWhichShelfAsync 功能
 * 负责实体书相关的业务逻辑处理
 */
public class PhysicalBookService {

    private final BookRepository repository = new BookRepository();

    /**
     * 根据书名关键词查询实体书信息
     * 
     * @param keyword 书名关键词
     * @return 实体书列表
     */
    public List<PhysicalBook> findPhysicalBooksByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return repository.findPhysicalBooksByKeyword(keyword.trim());
    }

    /**
     * 根据ISBN查询实体书信息
     * 
     * @param isbn ISBN号
     * @return 实体书列表
     */
    public List<PhysicalBook> findPhysicalBooksByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return List.of();
        }
        return repository.findPhysicalBooksByIsbn(isbn.trim());
    }

    /**
     * 根据书名或ISBN查询实体书信息
     * 如果提供了isbn则按isbn查询，否则按keyword查询
     * 
     * @param keyword 书名关键词（可选）
     * @param isbn ISBN号（可选）
     * @return 查询结果，包含实体书列表和书名
     */
    public PhysicalBookQueryResult queryPhysicalBooks(String keyword, String isbn) {
        List<PhysicalBook> books;
        String bookTitle = null;

        if (isbn != null && !isbn.isBlank()) {
            // 优先按ISBN查询
            books = findPhysicalBooksByIsbn(isbn);
            // 如果有结果，取第一本书的标题
            if (!books.isEmpty() && books.get(0).getTitle() != null) {
                bookTitle = books.get(0).getTitle();
            }
        } else if (keyword != null && !keyword.isBlank()) {
            // 按书名查询
            books = findPhysicalBooksByKeyword(keyword);
            bookTitle = keyword;
        } else {
            books = List.of();
        }

        return new PhysicalBookQueryResult(books, bookTitle != null ? bookTitle : "未知");
    }

    /**
     * 查询结果封装类
     */
    public static class PhysicalBookQueryResult {
        private final List<PhysicalBook> books;
        private final String bookTitle;

        public PhysicalBookQueryResult(List<PhysicalBook> books, String bookTitle) {
            this.books = books;
            this.bookTitle = bookTitle;
        }

        public List<PhysicalBook> getBooks() {
            return books;
        }

        public String getBookTitle() {
            return bookTitle;
        }
    }
}

