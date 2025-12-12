package com.example.demo0.admin.service;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.model.BookCategoryDetail;
import com.example.demo0.book.model.Category;
import com.example.demo0.book.repository.BookRepository;
import com.example.demo0.book.service.BookCategoryService;

import java.util.Collections;
import java.util.List;

/**
 * 管理员侧的图书分类绑定服务
 * 封装已有的 book 模块服务，提供简单的方法给管理员接口使用
 */
public class BookCategoryAdminService {

    private final BookRepository bookRepository;
    private final BookCategoryService bookCategoryService;

    public BookCategoryAdminService() {
        this.bookRepository = new BookRepository();
        this.bookCategoryService = new BookCategoryService();
    }

    /**
    * 按关键词搜索图书（标题/作者/ISBN）
    */
    public List<BookInfo> searchBooks(String keyword) {
        System.out.println("[BookCategoryAdminService] 搜索图书, keyword=" + keyword);
        return bookRepository.search(keyword);
    }

    /**
    * 获取叶子分类列表
    */
    public List<Category> getLeafCategories() {
        System.out.println("[BookCategoryAdminService] 获取叶子分类");
        return bookCategoryService.getLeafCategories();
    }

    /**
    * 获取某本书当前的分类绑定
    */
    public List<BookCategoryDetail> getBookCategories(String isbn) {
        System.out.println("[BookCategoryAdminService] 获取图书分类, ISBN=" + isbn);
        if (isbn == null || isbn.trim().isEmpty()) return Collections.emptyList();
        return bookCategoryService.getBookCategories(isbn.trim());
    }

    /**
    * 绑定图书到多个分类（覆盖式）
    */
    public boolean bindBookToCategories(String isbn, List<String> categoryIds, String operator) {
        System.out.println("[BookCategoryAdminService] 绑定图书分类, ISBN=" + isbn + ", 分类数量=" + (categoryIds != null ? categoryIds.size() : 0));
        return bookCategoryService.bindBookToCategories(isbn, categoryIds, null, operator);
    }
}

