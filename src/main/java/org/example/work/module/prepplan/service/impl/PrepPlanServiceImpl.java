package org.example.work.module.prepplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.model.chat.ChatModel;
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

import java.util.List;

@Service
public class PrepPlanServiceImpl implements PrepPlanService {

    private static final Logger log = LoggerFactory.getLogger(PrepPlanServiceImpl.class);

    private final LessonPlanMapper lessonPlanMapper;
    private final ExerciseMapper exerciseMapper;
    private final AnalyticsService analyticsService;
    private final ChatModel chatModel;

    public PrepPlanServiceImpl(LessonPlanMapper lessonPlanMapper,
                                ExerciseMapper exerciseMapper,
                                AnalyticsService analyticsService,
                                ChatModel chatModel) {
        this.lessonPlanMapper = lessonPlanMapper;
        this.exerciseMapper = exerciseMapper;
        this.analyticsService = analyticsService;
        this.chatModel = chatModel;
    }

    @Override
    public String generatePrepPlan(Long lessonPlanId, Long courseId) {
        log.info("生成总体备课方案: lessonPlanId={}, courseId={}", lessonPlanId, courseId);

        // 1. 获取教案
        LessonPlan lessonPlan = lessonPlanMapper.selectById(lessonPlanId);
        if (lessonPlan == null) {
            return "教案不存在";
        }

        // 2. 获取关联习题
        List<Exercise> exercises = exerciseMapper.selectList(
                new LambdaQueryWrapper<Exercise>().eq(Exercise::getLessonPlanId, lessonPlanId));

        // 3. 获取学情数据(如果有课程ID)
        ClassAnalyticsDTO analytics = null;
        if (courseId != null) {
            analytics = analyticsService.getClassAnalytics(courseId);
        }

        // 4. 组装 prompt
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

        String response = chatModel.chat(prompt.toString());
        log.info("备课方案生成完成");
        return response;
    }
}
