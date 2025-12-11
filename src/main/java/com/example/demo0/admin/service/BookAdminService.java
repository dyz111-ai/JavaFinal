package com.example.demo0.admin.service;

import com.example.demo0.admin.dto.AddCopiesDto;
import com.example.demo0.admin.dto.BookAdminDto;
import com.example.demo0.admin.dto.CreateBookDto;
import com.example.demo0.admin.dto.UpdateBookDto;
import com.example.demo0.admin.entity.Book;
import com.example.demo0.admin.entity.Bookinfo;
import com.example.demo0.admin.repository.BookAdminRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

@ApplicationScoped
@Transactional
public class BookAdminService {

    private static final Logger LOGGER = Logger.getLogger(BookAdminService.class);
    
    // 图书状态常量
    public static final String BOOK_STATUS_NORMAL = "正常";
    public static final String BOOK_STATUS_BORROWED = "借出";
    public static final String BOOK_STATUS_OFF_SHELF = "下架";
    
    @Inject
    private BookAdminRepository bookAdminRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    private BookAdminDto convertToDto(Bookinfo entity) {
        if (entity == null) {
            LOGGER.warn("尝试将空的Bookinfo实体转换为DTO");
            return null;
        }

        LOGGER.debug("将Bookinfo实体转换为DTO, ISBN: " + entity.getIsbn());
        BookAdminDto dto = new BookAdminDto();
        dto.setIsbn(entity.getIsbn());
        dto.setTitle(entity.getTitle());
        dto.setAuthor(entity.getAuthor());
        
        // 查询该ISBN对应的所有图书副本
        TypedQuery<Book> query = entityManager.createQuery(
            "SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class);
        query.setParameter("isbn", entity.getIsbn());
        List<Book> books = query.getResultList();
        
        // 计算各种状态的副本数量
        int totalCopies = books.size();
        int availableCopies = (int) books.stream()
            .filter(book -> BOOK_STATUS_NORMAL.equals(book.getStatus()))
            .count();
        int borrowedCopies = (int) books.stream()
            .filter(book -> BOOK_STATUS_BORROWED.equals(book.getStatus()))
            .count();
        int takedownCopies = (int) books.stream()
            .filter(book -> BOOK_STATUS_OFF_SHELF.equals(book.getStatus()))
            .count();
        
        // 设置正确的副本信息
        dto.setTotalCopies(totalCopies);
        dto.setPhysicalCopies(totalCopies); // 物理副本总数等于总副本数
        dto.setAvailableCopies(availableCopies);
        dto.setBorrowedCopies(borrowedCopies);
        dto.setTakedownCopies(takedownCopies);

        LOGGER.debug("Bookinfo转换完成, ISBN: " + entity.getIsbn() + ", 总副本数: " + totalCopies);
        return dto;
    }

    /**
     * 更新图书位置信息
     * @param isbn 图书ISBN
     * @param location 新的位置信息
     * @return 是否更新成功
     */
    public boolean updateBookLocation(String isbn, String location) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new IllegalArgumentException("图书ISBN不能为空");
            }
            
            if (location == null || location.trim().isEmpty()) {
                throw new IllegalArgumentException("图书位置不能为空");
            }
            
            LOGGER.info("更新图书位置，ISBN: " + isbn + "，新位置: " + location);
            boolean result = bookAdminRepository.updateBookLocation(isbn, location);
            
            LOGGER.info("图书位置更新" + (result ? "成功" : "失败") + "，ISBN: " + isbn);
            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.error("更新图书位置参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("更新图书位置失败", e);
            throw new RuntimeException("更新图书位置失败", e);
        }
    }

    public List<BookAdminDto> getBooks(String searchTerm) {
        try {
            LOGGER.info("搜索图书，关键词: " + searchTerm);
            
            List<Bookinfo> books = bookAdminRepository.searchBooks(searchTerm);
            List<BookAdminDto> result = books.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            LOGGER.info("图书搜索完成，找到 " + result.size() + " 本图书");
            return result;
        } catch (Exception e) {
            LOGGER.error("搜索图书失败", e);
            throw new RuntimeException("搜索图书失败", e);
        }
    }

    public void createBook(CreateBookDto dto) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("图书数据不能为空");
            }
            
            if (dto.getIsbn() == null || dto.getIsbn().trim().isEmpty()) {
                throw new IllegalArgumentException("图书ISBN不能为空");
            }
            
            boolean exists = bookAdminRepository.isIsbnExists(dto.getIsbn());
            if (exists) {
                LOGGER.warn("尝试添加重复ISBN: " + dto.getIsbn());
                throw new RuntimeException("该ISBN已存在于图书馆中: " + dto.getIsbn());
            }
            
            LOGGER.info("创建新图书: " + dto.getTitle() + " (ISBN: " + dto.getIsbn() + ")");
            bookAdminRepository.createBook(dto);
            LOGGER.info("图书创建成功: " + dto.getTitle());
        } catch (IllegalArgumentException e) {
            LOGGER.error("创建图书参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("创建图书失败", e);
            throw new RuntimeException("创建图书失败", e);
        }
    }

    public boolean updateBookInfo(String isbn, UpdateBookDto dto) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new IllegalArgumentException("图书ISBN不能为空");
            }
            
            if (dto == null) {
                throw new IllegalArgumentException("图书数据不能为空");
            }
            
            LOGGER.info("更新图书信息，ISBN: " + isbn);
            boolean result = bookAdminRepository.updateBookInfo(isbn, dto);
            
            LOGGER.info("图书更新" + (result ? "成功" : "失败") + "，ISBN: " + isbn);
            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.error("更新图书参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("更新图书信息失败", e);
            throw new RuntimeException("更新图书信息失败", e);
        }
    }

    public boolean takedownBook(String isbn) {
        try {
            if (isbn == null || isbn.trim().isEmpty()) {
                throw new IllegalArgumentException("图书ISBN不能为空");
            }
            
            LOGGER.info("下架图书，ISBN: " + isbn);
            boolean result = bookAdminRepository.takedownBook(isbn);
            
            LOGGER.info("图书下架" + (result ? "成功" : "失败") + "，ISBN: " + isbn);
            return result;
        } catch (IllegalArgumentException e) {
            LOGGER.error("下架图书参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("下架图书失败", e);
            throw new RuntimeException("下架图书失败", e);
        }
    }

    public void addCopies(AddCopiesDto dto) {
        try {
            if (dto == null) {
                throw new IllegalArgumentException("图书副本数据不能为空");
            }
            
            if (dto.getIsbn() == null || dto.getIsbn().trim().isEmpty()) {
                throw new IllegalArgumentException("图书ISBN不能为空");
            }
            
            if (dto.getNumberOfCopies() == null || dto.getNumberOfCopies() <= 0) {
                throw new IllegalArgumentException("副本数量必须大于0");
            }
            
            // 确保目标图书种类 (ISBN) 确实存在
            boolean exists = bookAdminRepository.isIsbnExists(dto.getIsbn());
            if (!exists) {
                LOGGER.warn("尝试为不存在的ISBN添加副本: " + dto.getIsbn());
                throw new RuntimeException("无法为不存在的ISBN添加入库，请先新增该图书种类。");
            }
            
            LOGGER.info("添加图书副本，ISBN: " + dto.getIsbn() + "，数量: " + dto.getNumberOfCopies());
            bookAdminRepository.addCopies(dto);
            LOGGER.info("添加图书副本成功，ISBN: " + dto.getIsbn() + "，数量: " + dto.getNumberOfCopies());
        } catch (IllegalArgumentException e) {
            LOGGER.error("添加图书副本参数验证失败", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("添加图书副本失败", e);
            throw new RuntimeException("添加图书副本失败", e);
        }
    }
}