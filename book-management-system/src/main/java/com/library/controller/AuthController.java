package com.library.controller;

import com.library.common.ServiceException;
import com.library.entity.User;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/index"})
    public String index(HttpSession session) {
        if (session.getAttribute("loginUser") != null) {
            return "redirect:/books";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = userService.login(username, password);
        if (user == null) {
            model.addAttribute("error", "用户名或密码错误");
            return "login";
        }
        session.setAttribute("loginUser", user);
        if ("admin".equals(user.getRole())) {
            return "redirect:/admin/books";
        }
        return "redirect:/books";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(required = false) String displayName,
                           @RequestParam(defaultValue = "reader") String role,
                           HttpSession session,
                           Model model) {
        try {
            userService.register(username, password, displayName, role);
        } catch (ServiceException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }

        // 注册成功后自动登录
        User user = userService.login(username, password);
        if (user != null) {
            session.setAttribute("loginUser", user);
        }
        return "redirect:/books";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
