package org.example.work.module.analytics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long courseId;
    private String examName;
    /** 考试类型: 1=随堂测验, 2=期中, 3=期末 */
    private Integer examType;
    private BigDecimal totalScore;
    private BigDecimal score;
    private LocalDate examDate;
    private LocalDateTime createTime;
}
