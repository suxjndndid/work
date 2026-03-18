package org.example.work.module.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import org.example.work.common.exception.BusinessException;
import org.example.work.module.auth.dto.LoginRequest;
import org.example.work.module.auth.dto.LoginResponse;
import org.example.work.module.auth.service.AuthService;
import org.example.work.module.user.entity.User;
import org.example.work.module.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        // 简单密码校验
        if (!request.getPassword().equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        return LoginResponse.of(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public User getCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        return userService.getById(userId);
    }
}
