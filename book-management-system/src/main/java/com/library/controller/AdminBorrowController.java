package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.entity.BorrowRecord;
import com.library.service.BorrowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 管理员查看全部借阅记录（仅管理员可访问，由拦截器控制）
 */
@Controller
@RequestMapping("/admin/borrows")
public class AdminBorrowController {

    private final BorrowService borrowService;

    public AdminBorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String status,
                       @RequestParam(defaultValue = "1") long page,
                       Model model) {
        if (page < 1) {
            page = 1;
        }
        Page<BorrowRecord> pageInfo = borrowService.pageAllRecords(page, 10, status, keyword);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/borrows";
    }
}
