package org.example.work.module.analytics.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ClassAnalyticsDTO {

    private Long courseId;
    private int studentCount;
    private BigDecimal averageScore;
    private BigDecimal medianScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal passRate;
    private BigDecimal excellentRate;

    /** 分数段分布: "0-59" -> 5, "60-69" -> 8, ... */
    private Map<String, Integer> scoreDistribution;

    /** 各次考试平均分趋势 */
    private List<ExamTrend> examTrends;

    @Data
    public static class ExamTrend {
        private String examName;
        private BigDecimal averageScore;
    }
}
