package org.example.work.module.prepplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import org.example.work.common.SseHelper;
import org.example.work.module.analytics.dto.ClassAnalyticsDTO;
import org.example.work.module.analytics.service.AnalyticsService;
import org.example.work.module.exercise.entity.Exercise;
import org.example.work.module.exercise.mapper.ExerciseMapper;
import org.example.work.module.lessonplan.entity.LessonPlan;
import org.example.work.module.lessonplan.mapper.LessonPlanMapper;
import org.example.work.module.prepplan.service.PrepPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
public class PrepPlanServiceImpl implements PrepPlanService {

    private static final Logger log = LoggerFactory.getLogger(PrepPlanServiceImpl.class);

    private final LessonPlanMapper lessonPlanMapper;
    private final ExerciseMapper exerciseMapper;
    private final AnalyticsService analyticsService;
    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public PrepPlanServiceImpl(LessonPlanMapper lessonPlanMapper,
                                ExerciseMapper exerciseMapper,
                                AnalyticsService analyticsService,
                                ChatModel chatModel,
                                StreamingChatModel streamingChatModel) {
        this.lessonPlanMapper = lessonPlanMapper;
        this.exerciseMapper = exerciseMapper;
        this.analyticsService = analyticsService;
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @Override
    public String generatePrepPlan(Long lessonPlanId, Long courseId) {
        log.info("生成总体备课方案: lessonPlanId={}, courseId={}", lessonPlanId, courseId);
        String prompt = buildPrompt(lessonPlanId, courseId);
        if (prompt == null) return "教案不存在";

        long startTime = System.currentTimeMillis();
        log.debug("[AI] 备课方案请求 - prompt长度={}", prompt.length());
        String response = chatModel.chat(prompt);
        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("[AI] 备课方案生成完成 - 耗时 {}ms, 返回长度 {}", elapsed, response != null ? response.length() : 0);
        log.info("备课方案生成完成");
        return response;
    }

    @Override
    public void streamPrepPlan(Long lessonPlanId, Long courseId, SseEmitter emitter) {
        log.info("流式生成备课方案: lessonPlanId={}, courseId={}", lessonPlanId, courseId);
        String prompt = buildPrompt(lessonPlanId, courseId);
        if (prompt == null) {
            try {
                emitter.send(SseEmitter.event().data("教案不存在"));
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
            return;
        }

        var messages = List.of(
                new SystemMessage("你是一位资深教学设计专家，请根据提供的教案、习题和学情数据生成备课方案。请使用中文输出，使用 Markdown 格式。"),
                new UserMessage(prompt)
        );
        SseHelper.streamChat(streamingChatModel, messages, emitter);
    }

    private String buildPrompt(Long lessonPlanId, Long courseId) {
        LessonPlan lessonPlan = lessonPlanMapper.selectById(lessonPlanId);
        if (lessonPlan == null) return null;

        List<Exercise> exercises = exerciseMapper.selectList(
                new LambdaQueryWrapper<Exercise>().eq(Exercise::getLessonPlanId, lessonPlanId));

        ClassAnalyticsDTO analytics = null;
        if (courseId != null) {
            analytics = analyticsService.getClassAnalytics(courseId);
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下资料，生成一份完整的备课方案，使用 Markdown 格式输出。\n\n");
        prompt.append("## 已有教案\n");
        prompt.append(lessonPlan.getContent()).append("\n\n");

        if (!exercises.isEmpty()) {
            prompt.append("## 已有练习题 (").append(exercises.size()).append("题)\n");
            for (int i = 0; i < exercises.size(); i++) {
                Exercise ex = exercises.get(i);
                prompt.append(i + 1).append(". [").append(ex.getQuestionType()).append("/").append(ex.getDifficulty()).append("] ");
                prompt.append(ex.getQuestionContent()).append("\n");
            }
            prompt.append("\n");
        }

        if (analytics != null && analytics.getStudentCount() > 0) {
            prompt.append("## 班级学情数据\n");
            prompt.append("- 学生人数: ").append(analytics.getStudentCount()).append("\n");
            prompt.append("- 平均分: ").append(analytics.getAverageScore()).append("\n");
            prompt.append("- 及格率: ").append(analytics.getPassRate()).append("%\n");
            prompt.append("- 优秀率: ").append(analytics.getExcellentRate()).append("%\n\n");
        }

        prompt.append("请输出一份包含以下内容的总体备课方案:\n");
        prompt.append("1. 备课方案概述\n");
        prompt.append("2. 教学设计要点(基于教案)\n");
        prompt.append("3. 教学资源清单(推荐的图片/视频/教具)\n");
        prompt.append("4. 练习与检测安排(基于习题)\n");
        prompt.append("5. 学情分析与教学策略调整(基于学情数据)\n");
        prompt.append("6. 课后延伸与个性化推荐\n");
        prompt.append("7. 备课时间安排建议\n");

        return prompt.toString();
    }
}
