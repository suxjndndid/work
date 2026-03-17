package org.example.work.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.work.common.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    /** 角色: 1=教师, 2=管理员 */
    private Integer role;
    private String avatar;
    private String email;
    /** 状态: 0=禁用, 1=启用 */
    private Integer status;
}
