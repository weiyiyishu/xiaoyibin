package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.ServiceException;
import com.library.entity.Book;
import com.library.entity.User;
import com.library.service.BookService;
import com.library.service.BorrowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@Controller
public class BookController {

    private static final List<String> CATEGORIES =
            Arrays.asList("计算机", "文学", "历史", "科学", "艺术", "经济", "其他");

    private final BookService bookService;
    private final BorrowService borrowService;

    public BookController(BookService bookService, BorrowService borrowService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
    }

    /**
     * 读者图书列表：支持关键词搜索 + 分类筛选 + 分页
     */
    @GetMapping("/books")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "1") long page,
                       Model model) {
        if (page < 1) {
            page = 1;
        }
        Page<Book> pageInfo = bookService.pageBooks(page, 12, keyword, category);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", CATEGORIES);
        return "books";
    }

    /**
     * 图书详情
     */
    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        try {
            Book book = bookService.getById(id);
            model.addAttribute("book", book);

            User user = (User) session.getAttribute("loginUser");
            boolean borrowing = user != null && borrowService.isBorrowing(user.getId(), id);
            model.addAttribute("borrowing", borrowing);
        } catch (ServiceException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
        return "book-detail";
    }
}
