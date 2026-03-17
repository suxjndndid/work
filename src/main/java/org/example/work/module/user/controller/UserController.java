package org.example.work.module.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.common.Result;
import org.example.work.module.user.entity.User;
import org.example.work.module.user.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getRealName, keyword).or().like(User::getUsername, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        userService.page(page, wrapper);
        // 隐藏密码
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(page);
    }

    @PostMapping
    public Result<?> create(@RequestBody User user) {
        // 简单密码加密 (生产环境应用 BCrypt)
        user.setPassword(String.valueOf(user.getPassword().hashCode()));
        userService.save(user);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null); // 不允许通过此接口修改密码
        userService.updateById(user);
        return Result.ok();
    }
}
