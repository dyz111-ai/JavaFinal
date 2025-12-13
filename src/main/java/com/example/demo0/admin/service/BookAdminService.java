package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.AddCopiesDto;
import com.example.demo0.admin.dto.BookAdminDto;
import com.example.demo0.admin.dto.CreateBookDto;
import com.example.demo0.admin.dto.UpdateBookDto;
import com.example.demo0.admin.repository.BookAdminRepository;
import com.example.demo0.util.CacheManager;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class BookAdminService {

    private BookAdminRepository repository;
    private CacheManager cacheManager;

    public BookAdminService() {
        try {
            repository = new BookAdminRepository();
            cacheManager = CacheManager.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("初始化失败: " + e.getMessage(), e);
        }
    }

    public List<BookAdminDto> getBooks(String search, int page, int pageSize) {
        if (repository == null) {
            return java.util.Collections.emptyList();
        }
        
        // 生成缓存键
        String cacheKey = "books_search:" + (search != null ? search : "") + ":page:" + page + ":pageSize:" + pageSize;
        
        // 尝试从缓存获取
        List<BookAdminDto> cachedResult = cacheManager.get(cacheKey);
        if (cachedResult != null) {
            System.out.println("[BookAdminService] ✅ 缓存命中: " + cacheKey);
            return cachedResult;
        }
        
        // 缓存未命中，从数据库查询
        try {
            System.out.println("[BookAdminService] ❌ 缓存未命中: " + cacheKey + "，从数据库查询");
            List<BookAdminDto> result = repository.searchBooks(search, page, pageSize);
            // 将结果存入缓存，有效期5分钟
            System.out.println("[BookAdminService] 📥 将查询结果存入缓存: " + cacheKey);
            cacheManager.put(cacheKey, result, 5, TimeUnit.MINUTES);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("查询失败", e);
        }
    }

    public int getTotalBooksCount(String search) {
        if (repository == null) {
            return 0;
        }
        
        // 生成缓存键
        String cacheKey = "books_count:" + (search != null ? search : "");
        
        // 尝试从缓存获取
        Integer cachedCount = cacheManager.get(cacheKey);
        if (cachedCount != null) {
            System.out.println("[BookAdminService] ✅ 缓存命中: " + cacheKey);
            return cachedCount;
        }
        
        // 缓存未命中，从数据库查询
        try {
            System.out.println("[BookAdminService] ❌ 缓存未命中: " + cacheKey + "，从数据库查询");
            int count = repository.getTotalBooksCount(search);
            // 将结果存入缓存，有效期5分钟
            System.out.println("[BookAdminService] 📥 将查询结果存入缓存: " + cacheKey);
            cacheManager.put(cacheKey, count, 5, TimeUnit.MINUTES);
            return count;
        } catch (Exception e) {
            throw new RuntimeException("获取总数失败", e);
        }
    }

    public void createBook(CreateBookDto dto) {
        repository.createBook(dto);
    }

    public boolean updateBookInfo(String isbn, UpdateBookDto dto) {
        return repository.updateBookInfo(isbn, dto);
    }

    public boolean updateBookLocation(Integer bookId, Integer buildingId, Integer floor, String zone) {
        System.out.println("[BookAdminService] ========== 更新图书位置 ==========");
        System.out.println("[BookAdminService] BookID: " + bookId);
        System.out.println("[BookAdminService] BuildingID: " + buildingId);
        System.out.println("[BookAdminService] Floor: " + floor);
        System.out.println("[BookAdminService] Zone: " + zone);
        return repository.updateBookLocation(bookId, buildingId, floor, zone);
    }

    public boolean takedownBook(Integer bookId) {
        System.out.println("[BookAdminService] ========== 下架图书 ==========");
        System.out.println("[BookAdminService] BookID: " + bookId);
        return repository.takedownBook(bookId);
    }

    public void addCopies(AddCopiesDto dto) {
        repository.addCopies(dto);
    }
}