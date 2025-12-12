package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.AddCopiesDto;
import com.example.demo0.admin.dto.BookAdminDto;
import com.example.demo0.admin.dto.CreateBookDto;
import com.example.demo0.admin.dto.UpdateBookDto;
import com.example.demo0.admin.repository.BookAdminRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class BookAdminService {

    private BookAdminRepository repository;

    public BookAdminService() {
        System.out.println("[BookAdminService] ========== Service构造函数 ==========");
        try {
            repository = new BookAdminRepository();
            System.out.println("[BookAdminService] ✅ BookAdminRepository 创建成功");
        } catch (Exception e) {
            System.err.println("[BookAdminService] ❌ BookAdminRepository 创建失败: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("[BookAdminService] ========== Service构造函数完成 ==========");
    }

    public List<BookAdminDto> getBooks(String search) {
        System.out.println("[BookAdminService] ========== getBooks 被调用 ==========");
        System.out.println("[BookAdminService] 搜索参数: " + search);
        System.out.println("[BookAdminService] Repository: " + (repository != null ? "已初始化" : "null"));
        
        if (repository == null) {
            System.err.println("[BookAdminService] ❌ Repository 为 null，无法执行查询");
            return java.util.Collections.emptyList();
        }
        
        try {
            List<BookAdminDto> result = repository.searchBooks(search);
            System.out.println("[BookAdminService] ✅ 查询完成，返回 " + result.size() + " 条记录");
            return result;
        } catch (Exception e) {
            System.err.println("[BookAdminService] ❌ 查询异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
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