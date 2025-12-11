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

    private final BookAdminRepository repository = new BookAdminRepository();

    public List<BookAdminDto> getBooks(String search) {
        return repository.searchBooks(search);
    }

    public void createBook(CreateBookDto dto) {
        repository.createBook(dto);
    }

    public boolean updateBookInfo(String isbn, UpdateBookDto dto) {
        return repository.updateBookInfo(isbn, dto);
    }

    public boolean updateBookLocation(String isbn, String location) {
        return repository.updateBookLocation(isbn, location);
    }

    public boolean takedownBook(String isbn) {
        return repository.takedownBook(isbn);
    }

    public void addCopies(AddCopiesDto dto) {
        repository.addCopies(dto);
    }
}