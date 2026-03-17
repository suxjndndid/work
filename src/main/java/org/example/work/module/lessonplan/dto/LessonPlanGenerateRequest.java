package org.example.work.module.lessonplan.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonPlanGenerateRequest {

    private Long courseId;

    @NotBlank(message = "学科不能为空")
    private String subject;

    @NotBlank(message = "年级不能为空")
    private String grade;

    @NotBlank(message = "课题不能为空")
    private String topic;

    private String objectives;

    @NotNull(message = "课时不能为空")
    private Integer duration;
}
