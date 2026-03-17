package org.example.work.module.exercise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExerciseGenerateRequest {

    private Long lessonPlanId;
    private Long courseId;

    @NotBlank(message = "主题不能为空")
    private String topic;

    @NotBlank(message = "题型不能为空")
    private String questionType;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotNull(message = "题目数量不能为空")
    private Integer count;
}
