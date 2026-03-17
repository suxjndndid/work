package org.example.work.module.auth.controller;

import jakarta.validation.Valid;
import org.example.work.common.Result;
import org.example.work.module.auth.dto.LoginRequest;
import org.example.work.module.auth.dto.LoginResponse;
import org.example.work.module.auth.service.AuthService;
import org.example.work.module.user.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        authService.logout();
        return Result.ok();
    }

    @GetMapping("/info")
    public Result<User> info() {
        User user = authService.getCurrentUser();
        user.setPassword(null);
        return Result.ok(user);
    }
}
