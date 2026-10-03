package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.library.common.ServiceException;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 注册：用户名唯一性校验 + 密码 BCrypt 加密
     */
    public void register(String username, String password, String displayName, String role) {
        if (username == null || username.trim().length() < 3) {
            throw new ServiceException("用户名至少 3 个字符");
        }
        if (password == null || password.length() < 6) {
            throw new ServiceException("密码至少 6 个字符");
        }
        if (role == null || (!"admin".equals(role) && !"reader".equals(role))) {
            role = "reader";
        }

        User exist = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username.trim()));
        if (exist != null) {
            throw new ServiceException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setDisplayName(displayName == null || displayName.trim().isEmpty() ? null : displayName.trim());
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
    }

    /**
     * 登录：校验用户名密码，成功返回用户，失败返回 null
     */
    public User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", username.trim()));
        if (user == null) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        // 脱敏：密码不回传给页面
        user.setPassword(null);
        return user;
    }
}
