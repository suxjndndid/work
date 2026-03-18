package org.example.work.module.exercise.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.work.module.ai.service.ExerciseAiService;
import org.example.work.module.exercise.dto.ExerciseGenerateRequest;
import org.example.work.module.exercise.entity.Exercise;
import org.example.work.module.exercise.mapper.ExerciseMapper;
import org.example.work.module.exercise.service.ExerciseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExerciseServiceImpl implements ExerciseService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ExerciseMapper exerciseMapper;
    private final ExerciseAiService exerciseAiService;

    public ExerciseServiceImpl(ExerciseMapper exerciseMapper, ExerciseAiService exerciseAiService) {
        this.exerciseMapper = exerciseMapper;
        this.exerciseAiService = exerciseAiService;
    }

    @Override
    public List<Exercise> generate(ExerciseGenerateRequest request) {
        log.info("AI 生成习题: topic={}, type={}, difficulty={}, count={}",
                request.getTopic(), request.getQuestionType(), request.getDifficulty(), request.getCount());

        long startTime = System.currentTimeMillis();
        log.debug("[AI] 习题生成请求发送...");

        String jsonResult = exerciseAiService.generateExercises(
                request.getTopic(),
                request.getQuestionType(),
                request.getDifficulty(),
                request.getCount()
        );

        long elapsed = System.currentTimeMillis() - startTime;
        log.debug("[AI] 习题生成响应 - 耗时 {}ms, 原始长度 {} 字符", elapsed, jsonResult != null ? jsonResult.length() : 0);
        log.debug("[AI] 习题原始返回: {}", jsonResult);

        // 从AI返回中提取JSON数组 — 兼容各种格式
        jsonResult = extractJsonArray(jsonResult);
        log.debug("[AI] 提取后的JSON: {}", jsonResult);

        List<Exercise> exercises = new ArrayList<>();
        try {
            List<Map<String, Object>> items = objectMapper.readValue(jsonResult, new TypeReference<>() {});
            for (Map<String, Object> item : items) {
                Exercise exercise = new Exercise();
                exercise.setLessonPlanId(request.getLessonPlanId());
                exercise.setCourseId(request.getCourseId());
                exercise.setTopic(request.getTopic());
                exercise.setQuestionType(request.getQuestionType());
                exercise.setDifficulty(request.getDifficulty());
                exercise.setQuestionContent((String) item.get("questionContent"));
                Object options = item.get("options");
                if (options != null) {
                    exercise.setOptions(objectMapper.writeValueAsString(options));
                }
                exercise.setAnswer((String) item.get("answer"));
                exercise.setExplanation((String) item.get("explanation"));
                exerciseMapper.insert(exercise);
                exercises.add(exercise);
            }
        } catch (Exception e) {
            log.error("解析 AI 生成的习题 JSON 失败: {}", e.getMessage());
            // 保存原始内容作为一道题
            Exercise fallback = new Exercise();
            fallback.setLessonPlanId(request.getLessonPlanId());
            fallback.setCourseId(request.getCourseId());
            fallback.setTopic(request.getTopic());
            fallback.setQuestionType(request.getQuestionType());
            fallback.setDifficulty(request.getDifficulty());
            fallback.setQuestionContent(jsonResult);
            fallback.setAnswer("AI 返回格式异常，请重新生成");
            exerciseMapper.insert(fallback);
            exercises.add(fallback);
        }

        log.info("习题生成完成, 共 {} 题", exercises.size());
        return exercises;
    }

    @Override
    public Page<Exercise> page(int current, int size, Long lessonPlanId, Long courseId) {
        Page<Exercise> page = new Page<>(current, size);
        LambdaQueryWrapper<Exercise> wrapper = new LambdaQueryWrapper<>();
        if (lessonPlanId != null) {
            wrapper.eq(Exercise::getLessonPlanId, lessonPlanId);
        }
        if (courseId != null) {
            wrapper.eq(Exercise::getCourseId, courseId);
        }
        wrapper.orderByDesc(Exercise::getCreateTime);
        return exerciseMapper.selectPage(page, wrapper);
    }

    @Override
    public void update(Exercise exercise) {
        exerciseMapper.updateById(exercise);
    }

    @Override
    public void delete(Long id) {
        exerciseMapper.deleteById(id);
    }

    /**
     * 从AI返回的文本中提取JSON数组，兼容以下格式：
     * 1. 纯JSON数组 [...]
     * 2. markdown代码块 ```json [...] ```
     * 3. 前后有多余文字但中间包含JSON数组
     */
    private String extractJsonArray(String raw) {
        if (raw == null || raw.isBlank()) {
            return "[]";
        }

        String text = raw.trim();

        // 1. 去除markdown代码块标记 ```json ... ``` 或 ``` ... ```
        if (text.contains("```")) {
            // 提取代码块内的内容
            int start = text.indexOf("```");
            // 跳过 ```json 或 ``` 这一行
            int contentStart = text.indexOf('\n', start);
            if (contentStart == -1) contentStart = start + 3;
            else contentStart += 1;

            int end = text.indexOf("```", contentStart);
            if (end > contentStart) {
                text = text.substring(contentStart, end).trim();
            } else {
                // 只有开头的 ```，去掉它
                text = text.substring(contentStart).trim();
            }
        }

        // 2. 找到第一个 [ 和最后一个 ] 来提取JSON数组
        int firstBracket = text.indexOf('[');
        int lastBracket = text.lastIndexOf(']');
        if (firstBracket != -1 && lastBracket > firstBracket) {
            text = text.substring(firstBracket, lastBracket + 1);
        }

        return text;
    }
}
