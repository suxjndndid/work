package org.example.work.module.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.work.common.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {

    private Long userId;
    private String name;
    private String subject;
    private String grade;
    private String semester;
    private String description;
}
