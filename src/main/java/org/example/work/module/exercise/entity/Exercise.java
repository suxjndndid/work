package org.example.work.module.exercise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.work.common.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exercise")
public class Exercise extends BaseEntity {

    private Long lessonPlanId;
    private Long courseId;
    private String topic;
    /** 题型: 选择题/填空题/判断题/简答题 */
    private String questionType;
    /** 难度: 简单/中等/困难 */
    private String difficulty;
    private String questionContent;
    /** 选项(JSON数组, 选择题用) */
    private String options;
    private String answer;
    private String explanation;
}
