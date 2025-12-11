package com.example.demo0.admin.repository;

import com.example.demo0.admin.entity.Book;
import com.example.demo0.admin.entity.Bookinfo;
import com.example.demo0.admin.dto.CreateBookDto;
import com.example.demo0.admin.dto.UpdateBookDto;
import com.example.demo0.admin.dto.AddCopiesDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BookAdminRepository {
    private static final Logger logger = Logger.getLogger(BookAdminRepository.class.getName());
    
    // 常量定义
    public static final String BOOK_STATUS_NORMAL = "正常";
    public static final String BOOK_STATUS_TAKEDOWN = "下架";

    @PersistenceContext
    private EntityManager entityManager;

    public List<Bookinfo> searchBooks(String searchTerm) {
        try {
            String jpql = "SELECT b FROM Bookinfo b WHERE LOWER(b.title) LIKE :term OR " +
                    "LOWER(b.author) LIKE :term OR b.isbn LIKE :term ORDER BY b.title";
            return entityManager.createQuery(jpql, Bookinfo.class)
                    .setParameter("term", "%" + (searchTerm != null ? searchTerm.toLowerCase() : "") + "%")
                    .getResultList();
        } catch (Exception e) {
            logger.error("搜索图书失败: " + searchTerm, e);
            return List.of();
        }
    }

    public boolean isIsbnExists(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        try {
            String jpql = "SELECT COUNT(b) FROM Bookinfo b WHERE b.isbn = :isbn";
            Long count = entityManager.createQuery(jpql, Long.class)
                    .setParameter("isbn", isbn)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            logger.error("检查ISBN是否存在失败: " + isbn, e);
            return false;
        }
    }

    public void createBook(CreateBookDto dto) { // 移除重复的@Transactional
        // 参数验证（更详细的错误信息）
        if (dto == null) {
            throw new IllegalArgumentException("图书数据不能为空");
        }
        if (dto.getIsbn() == null || dto.getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("图书标题不能为空");
        }
        if (dto.getNumberOfCopies() <= 0) {
            throw new IllegalArgumentException("图书数量必须大于0");
        }
        
        // 检查ISBN是否已存在
        if (isIsbnExists(dto.getIsbn())) {
            throw new IllegalArgumentException("ISBN已存在: " + dto.getIsbn());
        }

        try {
            // Step 1: Insert into Bookinfo
            Bookinfo bookinfo = new Bookinfo();
            bookinfo.setIsbn(dto.getIsbn());
            bookinfo.setTitle(dto.getTitle());
            bookinfo.setAuthor(dto.getAuthor());
            bookinfo.setStock(dto.getNumberOfCopies());
            entityManager.persist(bookinfo);

            // Step 2: Insert multiple copies into Book
            for (int i = 0; i < dto.getNumberOfCopies(); i++) {
                Book book = new Book();
                book.setIsbn(dto.getIsbn());
                book.setStatus(BOOK_STATUS_NORMAL);
                book.setBarcode(dto.getIsbn() + "-" + (i + 1));
                entityManager.persist(book);
            }
            
            logger.infof("创建图书成功: ISBN=%s, title=%s, 数量=%d", dto.getIsbn(), dto.getTitle(), dto.getNumberOfCopies());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("创建图书失败", e);
            throw new RuntimeException("创建图书失败: " + e.getMessage(), e);
        }
    }

    public boolean updateBookInfo(String isbn, UpdateBookDto dto) {
        if (isbn == null || dto == null) {
            logger.warn("更新图书信息参数无效: isbn或dto为空");
            return false;
        }
        
        try {
            Bookinfo bookinfo = entityManager.find(Bookinfo.class, isbn);
            if (bookinfo != null) {
                bookinfo.setTitle(dto.getTitle());
                bookinfo.setAuthor(dto.getAuthor());
                entityManager.merge(bookinfo);
                logger.infof("更新图书信息成功: ISBN=%s", isbn);
                return true;
            }
            logger.warn("未找到图书信息: ISBN=" + isbn);
            return false;
        } catch (Exception e) {
            logger.error("更新图书信息失败: ISBN=" + isbn, e);
            return false;
        }
    }

    public boolean takedownBook(String isbn) { // 移除重复的@Transactional
        if (isbn == null || isbn.trim().isEmpty()) {
            logger.warn("下架图书参数无效: ISBN为空");
            return false;
        }
        
        try {
            String jpql = "UPDATE Book b SET b.status = :takedownStatus WHERE b.isbn = :isbn AND b.status = :normalStatus";
            int affectedRows = entityManager.createQuery(jpql)
                    .setParameter("takedownStatus", BOOK_STATUS_TAKEDOWN)
                    .setParameter("isbn", isbn)
                    .setParameter("normalStatus", BOOK_STATUS_NORMAL)
                    .executeUpdate();
            
            if (affectedRows > 0) {
                logger.infof("下架图书成功: ISBN=%s, 影响行数=%d", isbn, affectedRows);
            } else {
                logger.warn("未找到可下架的图书: ISBN=" + isbn);
            }
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("下架图书失败: ISBN=" + isbn, e);
            return false;
        }
    }

    public void addCopies(AddCopiesDto dto) { // 移除重复的@Transactional
        // 参数验证
        if (dto == null) {
            throw new IllegalArgumentException("图书副本数据不能为空");
        }
        if (dto.getIsbn() == null || dto.getIsbn().trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN不能为空");
        }
        if (dto.getNumberOfCopies() <= 0) {
            throw new IllegalArgumentException("添加数量必须大于0");
        }
        if (dto.getShelfId() == null) {
            throw new IllegalArgumentException("书架ID不能为空");
        }

        try {
            // Step 1: 检查图书是否存在
            Bookinfo bookinfo = entityManager.find(Bookinfo.class, dto.getIsbn());
            if (bookinfo == null) {
                logger.warn("添加副本失败：图书不存在: " + dto.getIsbn());
                throw new IllegalArgumentException("图书不存在: " + dto.getIsbn());
            }

            int currentStock = bookinfo.getStock();

            // Step 2: Insert multiple copies into Book
            for (int i = 0; i < dto.getNumberOfCopies(); i++) {
                Book book = new Book();
                book.setIsbn(dto.getIsbn());
                book.setStatus(BOOK_STATUS_NORMAL);
                book.setShelfId(dto.getShelfId());
                book.setBarcode(dto.getIsbn() + "-" + (currentStock + i + 1));
                entityManager.persist(book);
            }

            // Step 3: Update Bookinfo stock
            bookinfo.setStock(currentStock + dto.getNumberOfCopies());
            entityManager.merge(bookinfo);
            
            logger.infof("添加图书副本成功: ISBN=%s, 添加数量=%d, 书架ID=%s", 
                    dto.getIsbn(), dto.getNumberOfCopies(), dto.getShelfId());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("添加图书副本失败", e);
            throw new RuntimeException("添加图书副本失败: " + e.getMessage(), e);
        }
    }

    public Bookinfo findBookInfoByIsbn(String isbn) {
        try {
            return entityManager.find(Bookinfo.class, isbn);
        } catch (Exception e) {
            logger.error("查找图书信息失败: ISBN=" + isbn, e);
            return null;
        }
    }
    
    /**
     * 更新图书位置信息
     * @param isbn 图书ISBN
     * @param location 新的位置信息
     * @return 是否更新成功
     */
    public boolean updateBookLocation(String isbn, String location) {
        if (isbn == null || location == null || location.trim().isEmpty()) {
            logger.warn("更新图书位置参数无效: ISBN或location为空");
            return false;
        }
        
        try {
            // 更新该ISBN下所有正常状态图书的位置
            String jpql = "UPDATE Book b SET b.location = :location WHERE b.isbn = :isbn AND b.status = :normalStatus";
            int affectedRows = entityManager.createQuery(jpql)
                    .setParameter("location", location)
                    .setParameter("isbn", isbn)
                    .setParameter("normalStatus", BOOK_STATUS_NORMAL)
                    .executeUpdate();
            
            if (affectedRows > 0) {
                logger.infof("更新图书位置成功: ISBN=%s, 新位置=%s, 影响行数=%d", isbn, location, affectedRows);
            } else {
                logger.warn("未找到可更新位置的图书: ISBN=" + isbn);
            }
            return affectedRows > 0;
        } catch (Exception e) {
            logger.error("更新图书位置失败: ISBN=" + isbn, e);
            return false;
        }
    }
}