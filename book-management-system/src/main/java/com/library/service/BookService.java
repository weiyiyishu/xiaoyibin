package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.ServiceException;
import com.library.entity.Book;
import com.library.mapper.BookMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class BookService {

    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    /**
     * 分页 + 关键词（书名/作者/ISBN）+ 分类筛选
     */
    public Page<Book> pageBooks(long page, long size, String keyword, String category) {
        QueryWrapper<Book> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                    .like("title", kw)
                    .or().like("author", kw)
                    .or().like("isbn", kw));
        }
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category.trim());
        }
        wrapper.orderByDesc("created_at");
        return bookMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Book getById(Long id) {
        if (id == null) {
            throw new ServiceException("图书不存在");
        }
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new ServiceException("图书不存在");
        }
        return book;
    }

    /**
     * 新增或更新图书（有 id 则更新，无 id 则新增）
     */
    public void save(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new ServiceException("书名不能为空");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new ServiceException("作者不能为空");
        }
        if (book.getStock() == null) {
            book.setStock(0);
        }

        if (book.getId() == null) {
            book.setCreatedAt(LocalDateTime.now());
            book.setUpdatedAt(LocalDateTime.now());
            bookMapper.insert(book);
        } else {
            book.setUpdatedAt(LocalDateTime.now());
            bookMapper.updateById(book);
        }
    }

    public void delete(Long id) {
        if (id == null || bookMapper.selectById(id) == null) {
            throw new ServiceException("图书不存在");
        }
        bookMapper.deleteById(id);
    }
}
