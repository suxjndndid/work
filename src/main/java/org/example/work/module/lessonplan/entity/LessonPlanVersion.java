package org.example.work.module.lessonplan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lesson_plan_version")
public class LessonPlanVersion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long lessonPlanId;
    private Integer versionNumber;
    private String content;
    private String changeNote;
    private LocalDateTime createTime;
}
