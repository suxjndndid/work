package org.example.work.module.analytics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.work.module.ai.service.AnalyticsAiService;
import org.example.work.module.analytics.dto.ClassAnalyticsDTO;
import org.example.work.module.analytics.dto.StudentAnalyticsDTO;
import org.example.work.module.analytics.entity.KnowledgePoint;
import org.example.work.module.analytics.entity.KnowledgePointMastery;
import org.example.work.module.analytics.entity.Score;
import org.example.work.module.analytics.entity.Student;
import org.example.work.module.analytics.mapper.KnowledgePointMapper;
import org.example.work.module.analytics.mapper.KnowledgePointMasteryMapper;
import org.example.work.module.analytics.mapper.ScoreMapper;
import org.example.work.module.analytics.mapper.StudentMapper;
import org.example.work.module.analytics.service.AnalyticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final KnowledgePointMasteryMapper masteryMapper;
    private final AnalyticsAiService analyticsAiService;

    public AnalyticsServiceImpl(ScoreMapper scoreMapper,
                                 StudentMapper studentMapper,
                                 KnowledgePointMapper knowledgePointMapper,
                                 KnowledgePointMasteryMapper masteryMapper,
                                 AnalyticsAiService analyticsAiService) {
        this.scoreMapper = scoreMapper;
        this.studentMapper = studentMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.masteryMapper = masteryMapper;
        this.analyticsAiService = analyticsAiService;
    }

    @Override
    public ClassAnalyticsDTO getClassAnalytics(Long courseId) {
        List<Score> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<Score>().eq(Score::getCourseId, courseId));

        ClassAnalyticsDTO dto = new ClassAnalyticsDTO();
        dto.setCourseId(courseId);

        if (scores.isEmpty()) {
            dto.setStudentCount(0);
            dto.setScoreDistribution(new LinkedHashMap<>());
            dto.setExamTrends(new ArrayList<>());
            return dto;
        }

        // 计算统计指标
        List<BigDecimal> allScores = scores.stream().map(Score::getScore).sorted().toList();
        dto.setStudentCount((int) scores.stream().map(Score::getStudentId).distinct().count());
        dto.setAverageScore(average(allScores));
        dto.setMedianScore(median(allScores));
        dto.setMaxScore(allScores.get(allScores.size() - 1));
        dto.setMinScore(allScores.get(0));

        long passCount = allScores.stream().filter(s -> s.compareTo(new BigDecimal("60")) >= 0).count();
        long excellentCount = allScores.stream().filter(s -> s.compareTo(new BigDecimal("90")) >= 0).count();
        dto.setPassRate(BigDecimal.valueOf(passCount * 100.0 / allScores.size()).setScale(1, RoundingMode.HALF_UP));
        dto.setExcellentRate(BigDecimal.valueOf(excellentCount * 100.0 / allScores.size()).setScale(1, RoundingMode.HALF_UP));

        // 分数段分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("0-59", 0);
        distribution.put("60-69", 0);
        distribution.put("70-79", 0);
        distribution.put("80-89", 0);
        distribution.put("90-100", 0);
        for (BigDecimal s : allScores) {
            int val = s.intValue();
            if (val < 60) distribution.merge("0-59", 1, Integer::sum);
            else if (val < 70) distribution.merge("60-69", 1, Integer::sum);
            else if (val < 80) distribution.merge("70-79", 1, Integer::sum);
            else if (val < 90) distribution.merge("80-89", 1, Integer::sum);
            else distribution.merge("90-100", 1, Integer::sum);
        }
        dto.setScoreDistribution(distribution);

        // 各次考试平均分趋势
        Map<String, List<BigDecimal>> examGroups = scores.stream()
                .filter(s -> s.getExamName() != null)
                .collect(Collectors.groupingBy(Score::getExamName,
                        LinkedHashMap::new,
                        Collectors.mapping(Score::getScore, Collectors.toList())));
        List<ClassAnalyticsDTO.ExamTrend> trends = new ArrayList<>();
        examGroups.forEach((name, scoreList) -> {
            ClassAnalyticsDTO.ExamTrend trend = new ClassAnalyticsDTO.ExamTrend();
            trend.setExamName(name);
            trend.setAverageScore(average(scoreList));
            trends.add(trend);
        });
        dto.setExamTrends(trends);

        return dto;
    }

    @Override
    public StudentAnalyticsDTO getStudentAnalytics(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        StudentAnalyticsDTO dto = new StudentAnalyticsDTO();
        dto.setStudentId(studentId);
        if (student != null) {
            dto.setStudentName(student.getName());
            dto.setStudentNo(student.getStudentNo());
        }

        // 该学生所有成绩
        List<Score> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<Score>().eq(Score::getStudentId, studentId).orderByAsc(Score::getExamDate));
        dto.setExamCount(scores.size());

        if (!scores.isEmpty()) {
            dto.setAverageScore(average(scores.stream().map(Score::getScore).toList()));
        }

        List<StudentAnalyticsDTO.ExamScore> examScores = scores.stream().map(s -> {
            StudentAnalyticsDTO.ExamScore es = new StudentAnalyticsDTO.ExamScore();
            es.setExamName(s.getExamName());
            es.setScore(s.getScore());
            es.setTotalScore(s.getTotalScore());
            es.setExamDate(s.getExamDate() != null ? s.getExamDate().toString() : null);
            return es;
        }).toList();
        dto.setExamScores(examScores);

        // 知识点掌握度
        List<KnowledgePointMastery> masteries = masteryMapper.selectList(
                new LambdaQueryWrapper<KnowledgePointMastery>().eq(KnowledgePointMastery::getStudentId, studentId));
        List<StudentAnalyticsDTO.KnowledgeMastery> kmList = masteries.stream().map(m -> {
            StudentAnalyticsDTO.KnowledgeMastery km = new StudentAnalyticsDTO.KnowledgeMastery();
            KnowledgePoint kp = knowledgePointMapper.selectById(m.getKnowledgePointId());
            km.setKnowledgePointName(kp != null ? kp.getName() : "未知");
            km.setMasteryLevel(m.getMasteryLevel());
            return km;
        }).toList();
        dto.setKnowledgeMasteries(kmList);

        return dto;
    }

    @Override
    public String generateClassReport(Long courseId) {
        ClassAnalyticsDTO analytics = getClassAnalytics(courseId);
        String data = formatClassData(analytics);
        return analyticsAiService.analyzeClassPerformance(data);
    }

    @Override
    public String generateStudentReport(Long studentId) {
        StudentAnalyticsDTO analytics = getStudentAnalytics(studentId);
        String data = formatStudentData(analytics);
        return analyticsAiService.analyzeStudentPerformance(data);
    }

    private String formatClassData(ClassAnalyticsDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("班级学情数据概览:\n");
        sb.append("- 学生人数: ").append(dto.getStudentCount()).append("\n");
        sb.append("- 平均分: ").append(dto.getAverageScore()).append("\n");
        sb.append("- 中位数: ").append(dto.getMedianScore()).append("\n");
        sb.append("- 最高分: ").append(dto.getMaxScore()).append("\n");
        sb.append("- 最低分: ").append(dto.getMinScore()).append("\n");
        sb.append("- 及格率: ").append(dto.getPassRate()).append("%\n");
        sb.append("- 优秀率: ").append(dto.getExcellentRate()).append("%\n\n");
        sb.append("分数段分布:\n");
        if (dto.getScoreDistribution() != null) {
            dto.getScoreDistribution().forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append("人\n"));
        }
        if (dto.getExamTrends() != null && !dto.getExamTrends().isEmpty()) {
            sb.append("\n各次考试平均分趋势:\n");
            dto.getExamTrends().forEach(t -> sb.append("- ").append(t.getExamName()).append(": ").append(t.getAverageScore()).append("\n"));
        }
        return sb.toString();
    }

    private String formatStudentData(StudentAnalyticsDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("学生: ").append(dto.getStudentName()).append(" (").append(dto.getStudentNo()).append(")\n");
        sb.append("平均分: ").append(dto.getAverageScore()).append("\n");
        sb.append("考试次数: ").append(dto.getExamCount()).append("\n\n");
        sb.append("历次考试成绩:\n");
        if (dto.getExamScores() != null) {
            dto.getExamScores().forEach(e -> sb.append("- ").append(e.getExamName())
                    .append(": ").append(e.getScore()).append("/").append(e.getTotalScore())
                    .append(" (").append(e.getExamDate()).append(")\n"));
        }
        if (dto.getKnowledgeMasteries() != null && !dto.getKnowledgeMasteries().isEmpty()) {
            sb.append("\n知识点掌握度:\n");
            dto.getKnowledgeMasteries().forEach(k -> sb.append("- ").append(k.getKnowledgePointName())
                    .append(": ").append(k.getMasteryLevel()).append("%\n"));
        }
        return sb.toString();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal median(List<BigDecimal> sorted) {
        if (sorted.isEmpty()) return BigDecimal.ZERO;
        int mid = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return sorted.get(mid - 1).add(sorted.get(mid)).divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
        }
        return sorted.get(mid);
    }
}
