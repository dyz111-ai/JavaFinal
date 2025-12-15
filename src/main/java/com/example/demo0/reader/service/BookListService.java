package com.example.demo0.reader.service;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.reader.model.BookList;

import com.example.demo0.reader.repository.BookListRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BookListService {

    private final BookListRepository repository = new BookListRepository();

    public BookList createBookList(String name, String introduction, Integer creatorId) throws SQLException {
        BookList booklist = new BookList();
        booklist.setBooklistName(name);
        booklist.setBooklistIntroduction(introduction);
        booklist.setCreatorId(creatorId);
        // 生成一个唯一的短码
        booklist.setListCode(UUID.randomUUID().toString().substring(0, 8));
        return repository.createBookList(booklist);
    }

    /**
     * 查询读者的书单（创建的和收藏的）
     * 返回格式：{ "Created": [...], "Collected": [...] }
     */
    public Map<String, List<BookList>> searchBooklistsByReader(Integer readerId) throws SQLException {
        List<BookList> created = repository.findCreatedBooklistsByReaderId(readerId);
        List<BookList> collected = repository.findCollectedBooklistsByReaderId(readerId);
        Map<String, List<BookList>> result = new HashMap<>();
        result.put("Created", created);
        result.put("Collected", collected);
        return result;
    }

    public BookList getBookListDetails(Integer booklistId) throws SQLException {
        return repository.findBookListById(booklistId);
    }

    public List<BookInfo> getBooksInBookList(Integer booklistId) throws SQLException {
        return repository.findBooksInBookList(booklistId);
    }

    public boolean deleteBookList(Integer booklistId, Integer readerId) throws SQLException {
        return repository.deleteBookList(booklistId) > 0;
    }

    public boolean addBookToBookList(Integer booklistId, String isbn, Integer readerId) throws SQLException {
        return repository.addBookToBookList(booklistId, isbn) > 0;
    }

    public boolean removeBookFromBookList(Integer booklistId, String isbn, Integer readerId) throws SQLException {
        return repository.removeBookFromBookList(booklistId, isbn) > 0;
    }

    public boolean updateBookListInfo(Integer booklistId, String name, String introduction, Integer readerId) throws SQLException {
        return repository.updateBookListInfo(booklistId, name, introduction) > 0;
    }

    /**
     * 推荐相似书单
     */
    public List<BookList> recommendBooklists(Integer booklistId, Integer limit) throws SQLException {
        return repository.recommendBooklists(booklistId, limit != null ? limit : 10);
    }

    /**
     * 收藏书单
     */
    public boolean collectBooklist(Integer booklistId, Integer readerId, String notes) throws SQLException {
        return repository.collectBooklist(booklistId, readerId, notes) > 0;
    }

    /**
     * 取消收藏书单
     */
    public boolean cancelCollectBooklist(Integer booklistId, Integer readerId) throws SQLException {
        return repository.cancelCollectBooklist(booklistId, readerId) > 0;
    }

    /**
     * 更新收藏备注
     */
    public boolean updateCollectNotes(Integer booklistId, Integer readerId, String newNotes) throws SQLException {
        return repository.updateCollectNotes(booklistId, readerId, newNotes) > 0;
    }

    /**
     * 只更新书单名称
     */
    public boolean updateBooklistName(Integer booklistId, String name, Integer readerId) throws SQLException {
        return repository.updateBooklistName(booklistId, name, readerId) > 0;
    }

    /**
     * 只更新书单简介
     */
    public boolean updateBooklistIntroduction(Integer booklistId, String introduction, Integer readerId) throws SQLException {
        return repository.updateBooklistIntroduction(booklistId, introduction, readerId) > 0;
    }
}
