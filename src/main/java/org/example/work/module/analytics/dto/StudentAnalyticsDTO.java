package org.example.work.module.analytics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentAnalyticsDTO {

    private Long studentId;
    private String studentName;
    private String studentNo;
    private BigDecimal averageScore;
    private int examCount;

    /** 各次考试成绩 */
    private List<ExamScore> examScores;

    /** 知识点掌握度 */
    private List<KnowledgeMastery> knowledgeMasteries;

    @Data
    public static class ExamScore {
        private String examName;
        private BigDecimal score;
        private BigDecimal totalScore;
        private String examDate;
    }

    @Data
    public static class KnowledgeMastery {
        private String knowledgePointName;
        private BigDecimal masteryLevel;
    }
}
