package org.example.work.module.analytics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_point_mastery")
public class KnowledgePointMastery {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long knowledgePointId;
    private BigDecimal masteryLevel;
    private Integer assessmentCount;
    private LocalDate lastAssessmentDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
