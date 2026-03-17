package org.example.work.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.work.module.user.entity.User;

public interface UserService extends IService<User> {

    User getByUsername(String username);
}
