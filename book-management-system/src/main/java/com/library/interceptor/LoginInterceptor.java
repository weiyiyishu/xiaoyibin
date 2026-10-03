package com.library.interceptor;

import com.library.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器：未登录跳转登录页；非管理员访问 /admin 跳转图书列表
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        String uri = request.getRequestURI();

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        if (uri.startsWith("/admin/") && !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/books");
            return false;
        }

        return true;
    }
}
