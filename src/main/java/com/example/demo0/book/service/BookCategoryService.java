package com.example.demo0.book.service;

import com.example.demo0.book.model.BookCategory;
import com.example.demo0.book.model.BookCategoryDetail;
import com.example.demo0.book.model.Category;
import com.example.demo0.book.repository.BookCategoryRepository;
import com.example.demo0.book.repository.BookRepository;
import com.example.demo0.book.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图书分类关联服务层
 * 对应原项目的 BookCategoryService
 */
public class BookCategoryService {

    private final BookCategoryRepository bookCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public BookCategoryService() {
        this.bookCategoryRepository = new BookCategoryRepository();
        this.categoryRepository = new CategoryRepository();
        this.bookRepository = new BookRepository();
    }

    /**
     * 绑定图书到多个分类
     */
    public boolean bindBookToCategories(String isbn, List<String> categoryIds, String relationNote, String operatorId) {
        // 验证图书是否存在
        if (bookRepository.findByIsbn(isbn) == null) {
            throw new IllegalArgumentException("图书 ISBN " + isbn + " 不存在");
        }
        
        // 验证所有分类是否为叶子节点
        for (String categoryId : categoryIds) {
            if (!bookCategoryRepository.isLeafCategory(categoryId)) {
                Category category = categoryRepository.findById(categoryId);
                String categoryName = category != null ? category.getCategoryName() : categoryId;
                throw new IllegalArgumentException("分类 '" + categoryName + "' 不是叶子节点，无法绑定图书");
            }
        }
        
        // 先删除现有的分类关联
        bookCategoryRepository.removeAllByIsbn(isbn);
        
        // 添加新的分类关联
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<BookCategory> bookCategories = categoryIds.stream()
                .map(categoryId -> new BookCategory(isbn, categoryId, relationNote))
                .collect(Collectors.toList());
            bookCategoryRepository.addAll(bookCategories);
        }
        
        return true;
    }

    /**
     * 添加单个图书分类关联
     */
    public boolean addBookCategory(String isbn, String categoryId, String relationNote, String operatorId) {
        // 验证图书是否存在
        if (bookRepository.findByIsbn(isbn) == null) {
            throw new IllegalArgumentException("图书 ISBN " + isbn + " 不存在");
        }
        
        // 验证分类是否存在
        Category category = categoryRepository.findById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        
        // 验证分类是否为叶子节点
        if (!bookCategoryRepository.isLeafCategory(categoryId)) {
            throw new IllegalArgumentException("分类 '" + category.getCategoryName() + "' 不是叶子节点，无法绑定图书");
        }
        
        // 检查关联是否已存在
        if (bookCategoryRepository.exists(isbn, categoryId)) {
            throw new IllegalArgumentException("该图书已关联到此分类");
        }
        
        // 添加关联
        BookCategory bookCategory = new BookCategory(isbn, categoryId, relationNote);
        return bookCategoryRepository.add(bookCategory) > 0;
    }

    /**
     * 移除图书分类关联
     */
    public boolean removeBookCategory(String isbn, String categoryId, String operatorId) {
        // 检查关联是否存在
        if (!bookCategoryRepository.exists(isbn, categoryId)) {
            throw new IllegalArgumentException("该图书分类关联不存在");
        }
        
        return bookCategoryRepository.remove(isbn, categoryId) > 0;
    }

    /**
     * 获取图书的所有分类关联
     */
    public List<BookCategoryDetail> getBookCategories(String isbn) {
        return bookCategoryRepository.findByIsbn(isbn);
    }

    /**
     * 获取分类的所有图书关联
     */
    public List<BookCategoryDetail> getCategoryBooks(String categoryId) {
        return bookCategoryRepository.findByCategoryId(categoryId);
    }

    /**
     * 获取所有叶子节点分类（用于绑定选择）
     */
    public List<Category> getLeafCategories() {
        return bookCategoryRepository.findLeafCategories();
    }

    /**
     * 获取分类路径
     */
    public List<String> getCategoryPath(String categoryId) {
        return categoryRepository.getCategoryPath(categoryId);
    }
}

