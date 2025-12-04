package com.example.demo0.book.service;

import com.example.demo0.book.model.BookInfo;
import com.example.demo0.book.repository.BookRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BookSearchService {

    private final BookRepository repository = new BookRepository();

    public List<BookInfo> search(String keyword) {
        return repository.search(keyword);
    }

    public List<BookInfo> searchAndFilter(String keyword, String category) {
        List<BookInfo> list = repository.search(keyword);
        if (category == null || category.isBlank() || "全部".equals(category)) return list;
        final String c = category.trim();
        return list.stream().filter(b -> b.getCategories().contains(c)).collect(Collectors.toList());
    }

    public List<String> extractCategories(List<BookInfo> books) {
        if (books == null || books.isEmpty()) return Collections.emptyList();
        Set<String> set = new LinkedHashSet<>();
        for (BookInfo b : books) {
            if (b.getCategories() != null) set.addAll(b.getCategories());
        }
        return new ArrayList<>(set);
    }
}
