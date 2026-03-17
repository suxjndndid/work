package org.example.work.module.auth.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private Integer role;

    public static LoginResponse of(String token, Long userId, String username, String realName, Integer role) {
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(userId);
        resp.setUsername(username);
        resp.setRealName(realName);
        resp.setRole(role);
        return resp;
    }
}
