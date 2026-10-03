package com.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.ServiceException;
import com.library.entity.Book;
import com.library.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员图书管理（仅管理员可访问，由拦截器控制）
 */
@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private static final List<String> CATEGORIES =
            Arrays.asList("计算机", "文学", "历史", "科学", "艺术", "经济", "其他");

    private static final List<String> ALLOWED_IMG_EXT =
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    private final BookService bookService;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public AdminBookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 管理后台列表
     */
    @GetMapping("")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "1") long page,
                       Model model) {
        if (page < 1) {
            page = 1;
        }
        Page<Book> pageInfo = bookService.pageBooks(page, 10, keyword, category);
        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("categories", CATEGORIES);
        return "admin/books";
    }

    /**
     * 新增 / 编辑表单（id 为空则新增）
     */
    @GetMapping("/form")
    public String form(@RequestParam(required = false) Long id, Model model) {
        Book book = new Book();
        if (id != null) {
            try {
                book = bookService.getById(id);
            } catch (ServiceException e) {
                model.addAttribute("error", e.getMessage());
                return "error";
            }
        }
        model.addAttribute("book", book);
        model.addAttribute("categories", CATEGORIES);
        return "admin/book-form";
    }

    /**
     * 保存（新增或更新）
     */
    @PostMapping("/save")
    public String save(Book book, RedirectAttributes ra) {
        try {
            bookService.save(book);
            ra.addFlashAttribute("success", book.getId() == null ? "图书添加成功" : "图书更新成功");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Long id, RedirectAttributes ra) {
        try {
            bookService.delete(id);
            ra.addFlashAttribute("success", "删除成功");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/books";
    }

    /**
     * 封面图片上传：表单页点击图片后通过 AJAX 调用，返回图片访问 URL
     */
    @PostMapping("/upload-cover")
    @ResponseBody
    public Map<String, Object> uploadCover(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file == null || file.isEmpty()) {
                throw new ServiceException("请选择要上传的图片");
            }
            String contentType = file.getContentType();
            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null) {
                int dot = original.lastIndexOf('.');
                if (dot >= 0) {
                    ext = original.substring(dot + 1).toLowerCase();
                }
            }
            if (contentType == null || !contentType.startsWith("image/")
                    || !ALLOWED_IMG_EXT.contains(ext)) {
                throw new ServiceException("仅支持 JPG / PNG / GIF / WEBP 格式的图片");
            }
            if (file.getSize() > 5L * 1024 * 1024) {
                throw new ServiceException("图片大小不能超过 5MB");
            }

            Path dirPath = Paths.get(uploadDir, "books").toAbsolutePath();
            Files.createDirectories(dirPath);

            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            file.transferTo(dirPath.resolve(filename).toFile());

            result.put("success", true);
            result.put("url", "/uploads/books/" + filename);
        } catch (ServiceException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "图片上传失败，请重试");
        }
        return result;
    }
}
