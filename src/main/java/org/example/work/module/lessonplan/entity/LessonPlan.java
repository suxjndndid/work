package org.example.work.module.lessonplan.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.work.common.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lesson_plan")
public class LessonPlan extends BaseEntity {

    private Long userId;
    private Long courseId;
    private String title;
    private String subject;
    private String grade;
    private String topic;
    private String objectives;
    private Integer duration;
    private String content;
    private String teachingGoals;
    private String keyPoints;
    private String difficultPoints;
    private String teachingProcess;
    private String activities;
    private String assessment;
    /** 0=草稿, 1=已发布 */
    private Integer status;
    private Integer version;
}
