package com.library.controller;

import com.library.common.ServiceException;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.service.BorrowService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    /**
     * 借书：从图书详情页发起，完成后回到详情页并提示
     */
    @PostMapping("/borrow/{bookId}")
    public String borrow(@PathVariable Long bookId,
                         HttpSession session,
                         RedirectAttributes ra) {
        User user = (User) session.getAttribute("loginUser");
        try {
            borrowService.borrowBook(user.getId(), bookId);
            ra.addFlashAttribute("success", "借阅成功，请在 30 天内归还");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books/" + bookId;
    }

    /**
     * 我的借阅：借阅中 + 历史记录
     */
    @GetMapping("/my/borrows")
    public String myBorrows(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loginUser");
        List<BorrowRecord> records = borrowService.listMyRecords(user.getId());

        List<BorrowRecord> active = records.stream()
                .filter(r -> BorrowRecord.STATUS_BORROWING.equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        List<BorrowRecord> history = records.stream()
                .filter(r -> BorrowRecord.STATUS_RETURNED.equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("activeRecords", active);
        model.addAttribute("historyRecords", history);
        return "my-borrows";
    }

    /**
     * 还书：从“我的借阅”页发起
     */
    @PostMapping("/borrow/return/{recordId}")
    public String returnBook(@PathVariable Long recordId,
                             HttpSession session,
                             RedirectAttributes ra) {
        User user = (User) session.getAttribute("loginUser");
        try {
            borrowService.returnBook(user.getId(), recordId);
            ra.addFlashAttribute("success", "归还成功，感谢您的阅读");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my/borrows";
    }
}
