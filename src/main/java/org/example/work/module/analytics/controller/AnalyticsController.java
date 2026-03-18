package org.example.work.module.analytics.controller;

import org.example.work.common.Result;
import org.example.work.module.ai.service.ImageAiService;
import org.example.work.module.analytics.dto.ClassAnalyticsDTO;
import org.example.work.module.analytics.dto.StudentAnalyticsDTO;
import org.example.work.module.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;
    private final ImageAiService imageAiService;

    public AnalyticsController(AnalyticsService analyticsService, ImageAiService imageAiService) {
        this.analyticsService = analyticsService;
        this.imageAiService = imageAiService;
    }

    @GetMapping("/class/{courseId}")
    public Result<ClassAnalyticsDTO> classAnalytics(@PathVariable Long courseId) {
        return Result.ok(analyticsService.getClassAnalytics(courseId));
    }

    @GetMapping("/student/{studentId}")
    public Result<StudentAnalyticsDTO> studentAnalytics(@PathVariable Long studentId) {
        return Result.ok(analyticsService.getStudentAnalytics(studentId));
    }

    @PostMapping("/ai-report/{courseId}")
    public Result<String> classAiReport(@PathVariable Long courseId) {
        return Result.ok(analyticsService.generateClassReport(courseId));
    }

    @PostMapping("/ai-student-report/{studentId}")
    public Result<String> studentAiReport(@PathVariable Long studentId) {
        return Result.ok(analyticsService.generateStudentReport(studentId));
    }

    /** AI 个性化资源推荐 */
    @PostMapping("/recommend/{studentId}")
    public Result<String> recommend(@PathVariable Long studentId) {
        log.debug("[AI] 个性化推荐请求 - studentId={}", studentId);
        long startTime = System.currentTimeMillis();
        StudentAnalyticsDTO analytics = analyticsService.getStudentAnalytics(studentId);
        StringBuilder sb = new StringBuilder();
        sb.append("学生: ").append(analytics.getStudentName()).append("\n");
        sb.append("平均分: ").append(analytics.getAverageScore()).append("\n");
        if (analytics.getKnowledgeMasteries() != null) {
            sb.append("知识点掌握度:\n");
            analytics.getKnowledgeMasteries().forEach(k ->
                    sb.append("- ").append(k.getKnowledgePointName()).append(": ").append(k.getMasteryLevel()).append("%\n"));
        }
        if (analytics.getExamScores() != null) {
            sb.append("历次成绩:\n");
            analytics.getExamScores().forEach(e ->
                    sb.append("- ").append(e.getExamName()).append(": ").append(e.getScore()).append("/").append(e.getTotalScore()).append("\n"));
        }
        String recommendation = imageAiService.recommendResources(sb.toString());
        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("[AI] 个性化推荐完成 - studentId={}, 耗时 {}ms, 返回长度 {}", studentId, elapsed, recommendation != null ? recommendation.length() : 0);
        return Result.ok(recommendation);
    }
}
