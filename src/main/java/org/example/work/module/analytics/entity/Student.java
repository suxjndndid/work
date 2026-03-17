package org.example.work.module.analytics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.work.common.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    private String studentNo;
    private String name;
    /** 性别: 0=女, 1=男 */
    private Integer gender;
    private String className;
    private String grade;
}
