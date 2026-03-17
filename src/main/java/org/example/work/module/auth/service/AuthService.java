package org.example.work.module.auth.service;

import org.example.work.module.auth.dto.LoginRequest;
import org.example.work.module.auth.dto.LoginResponse;
import org.example.work.module.user.entity.User;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    User getCurrentUser();
}
