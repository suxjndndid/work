package org.example.work.module.analytics.controller;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.example.work.common.Result;
import org.example.work.common.SseHelper;
import org.example.work.module.analytics.dto.ClassAnalyticsDTO;
import org.example.work.module.analytics.dto.StudentAnalyticsDTO;
import org.example.work.module.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;
    private final StreamingChatModel streamingChatModel;

    public AnalyticsController(AnalyticsService analyticsService, StreamingChatModel streamingChatModel) {
        this.analyticsService = analyticsService;
        this.streamingChatModel = streamingChatModel;
    }

    @GetMapping("/class/{courseId}")
    public Result<ClassAnalyticsDTO> classAnalytics(@PathVariable Long courseId) {
        return Result.ok(analyticsService.getClassAnalytics(courseId));
    }

    @GetMapping("/student/{studentId}")
    public Result<StudentAnalyticsDTO> studentAnalytics(@PathVariable Long studentId) {
        return Result.ok(analyticsService.getStudentAnalytics(studentId));
    }

    @PostMapping(value = "/ai-report/{courseId}", produces = "text/event-stream")
    public SseEmitter classAiReport(@PathVariable Long courseId) {
        SseEmitter emitter = SseHelper.createEmitter();
        analyticsService.streamClassReport(courseId, emitter);
        return emitter;
    }

    @PostMapping(value = "/ai-student-report/{studentId}", produces = "text/event-stream")
    public SseEmitter studentAiReport(@PathVariable Long studentId) {
        SseEmitter emitter = SseHelper.createEmitter();
        analyticsService.streamStudentReport(studentId, emitter);
        return emitter;
    }

    /** AI 个性化资源推荐（流式） */
    @PostMapping(value = "/recommend/{studentId}", produces = "text/event-stream")
    public SseEmitter recommend(@PathVariable Long studentId) {
        log.debug("[AI-Stream] 个性化推荐请求 - studentId={}", studentId);
        SseEmitter emitter = SseHelper.createEmitter();

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

        var messages = List.of(
                new SystemMessage("你是一位教育资源推荐专家，擅长根据学生的学情分析结果推荐个性化的学习资源。\n请使用中文输出，使用 Markdown 格式。"),
                new UserMessage("根据以下学情分析数据，为该学生推荐个性化的预习或复习资源:\n" + sb + "\n\n请提供:\n1. 需要重点复习的知识点及推荐资源\n2. 推荐的预习内容\n3. 推荐的练习方向和题型\n4. 学习方法建议")
        );
        SseHelper.streamChat(streamingChatModel, messages, emitter);
        return emitter;
    }
}
