package org.example.work.module.lessonplan.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import org.example.work.common.Result;
import org.example.work.module.ai.service.ImageAiService;
import org.example.work.module.lessonplan.dto.LessonPlanGenerateRequest;
import org.example.work.module.lessonplan.entity.LessonPlan;
import org.example.work.module.lessonplan.entity.LessonPlanVersion;
import org.example.work.module.lessonplan.service.LessonPlanService;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@RestController
@RequestMapping("/api/lesson-plan")
public class LessonPlanController {

    private final LessonPlanService lessonPlanService;
    private final ImageAiService imageAiService;

    public LessonPlanController(LessonPlanService lessonPlanService, ImageAiService imageAiService) {
        this.lessonPlanService = lessonPlanService;
        this.imageAiService = imageAiService;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int current,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(lessonPlanService.page(current, size, userId, keyword));
    }

    @GetMapping("/{id}")
    public Result<LessonPlan> getById(@PathVariable Long id) {
        return Result.ok(lessonPlanService.getById(id));
    }

    @PostMapping("/generate")
    public Result<LessonPlan> generate(@Valid @RequestBody LessonPlanGenerateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(lessonPlanService.generate(request, userId));
    }

    @PostMapping("/{id}/regenerate")
    public Result<LessonPlan> regenerate(@PathVariable Long id) {
        return Result.ok(lessonPlanService.regenerate(id));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody LessonPlan lessonPlan) {
        lessonPlan.setId(id);
        lessonPlanService.update(lessonPlan);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        lessonPlanService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/versions")
    public Result<List<LessonPlanVersion>> versions(@PathVariable Long id) {
        return Result.ok(lessonPlanService.getVersions(id));
    }

    /** 互动环节模板库 */
    @GetMapping("/templates")
    public Result<List<Map<String, String>>> templates() {
        List<Map<String, String>> templates = List.of(
                Map.of("name", "小组讨论", "description", "学生分组围绕核心问题展开讨论，每组派代表汇报",
                        "duration", "10", "template", "### 小组讨论\n- **主题**: [讨论主题]\n- **分组**: 每组4-5人\n- **时间**: 10分钟\n- **要求**: 每组选出记录员和汇报员\n- **汇报**: 每组2分钟汇报讨论成果"),
                Map.of("name", "角色扮演", "description", "学生扮演不同角色模拟真实场景，加深理解",
                        "duration", "15", "template", "### 角色扮演\n- **场景**: [场景描述]\n- **角色分配**: [角色列表]\n- **时间**: 15分钟\n- **观众任务**: 观察并记录关键表现\n- **总结**: 全班讨论表演中的知识点"),
                Map.of("name", "案例分析", "description", "通过真实案例引导学生分析问题、解决问题",
                        "duration", "10", "template", "### 案例分析\n- **案例**: [案例内容]\n- **问题**: 请从[角度]分析该案例\n- **时间**: 10分钟独立思考 + 5分钟分享\n- **目标**: 培养批判性思维和问题解决能力"),
                Map.of("name", "课堂抢答", "description", "教师提问，学生举手抢答，活跃课堂氛围",
                        "duration", "5", "template", "### 课堂抢答\n- **规则**: 教师提问后举手抢答\n- **题目数**: 5-8题\n- **时间**: 5分钟\n- **奖励**: 回答正确加平时分\n- **覆盖**: 本节课重点知识"),
                Map.of("name", "思维导图", "description", "学生绘制思维导图梳理知识结构",
                        "duration", "8", "template", "### 思维导图绘制\n- **主题**: [中心主题]\n- **要求**: 至少3个分支，每分支2-3个要点\n- **时间**: 8分钟\n- **工具**: 纸笔或平板\n- **展示**: 选取优秀作品全班展示"),
                Map.of("name", "随堂练习", "description", "课堂当场完成练习题，即时检测学习效果",
                        "duration", "10", "template", "### 随堂练习\n- **题量**: 3-5题\n- **题型**: [题型]\n- **时间**: 10分钟\n- **方式**: 独立完成后同桌互批\n- **讲评**: 教师重点讲解错误率高的题目")
        );
        return Result.ok(templates);
    }

    /** AI 提取教案图片关键词 */
    @PostMapping("/{id}/keywords")
    public Result<List<String>> extractKeywords(@PathVariable Long id) {
        LessonPlan plan = lessonPlanService.getById(id);
        if (plan == null || plan.getContent() == null) {
            return Result.error("教案不存在或无内容");
        }
        String raw = imageAiService.extractImageKeywords(plan.getContent());
        // 解析AI返回的JSON，提取keyword字段
        List<String> keywordList = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 去除可能的markdown代码块标记
            String json = raw.trim();
            if (json.contains("```")) {
                int start = json.indexOf('\n', json.indexOf("```"));
                int end = json.indexOf("```", start);
                if (start > 0 && end > start) json = json.substring(start, end).trim();
            }
            int first = json.indexOf('[');
            int last = json.lastIndexOf(']');
            if (first >= 0 && last > first) {
                json = json.substring(first, last + 1);
            }
            List<Map<String, Object>> items = mapper.readValue(json, new TypeReference<>() {});
            for (Map<String, Object> item : items) {
                Object kw = item.get("keyword");
                if (kw != null) keywordList.add(kw.toString());
            }
        } catch (Exception e) {
            // 解析失败时，尝试按逗号或换行分割原始文本
            for (String s : raw.replace("[", "").replace("]", "").replace("\"", "").split("[,，\\n]")) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && trimmed.length() < 50) keywordList.add(trimmed);
            }
        }
        return Result.ok(keywordList);
    }

    /** AI 生成教学图表（Mermaid） */
    @PostMapping("/{id}/generate-image")
    public Result<Map<String, String>> generateImage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        if (keyword == null || keyword.isBlank()) {
            return Result.error("关键词不能为空");
        }
        String raw = imageAiService.generateDiagram(keyword);
        // 解析AI返回的JSON
        Map<String, String> result = new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = raw.trim();
            // 去除markdown代码块
            if (json.contains("```")) {
                int start = json.indexOf('\n', json.indexOf("```"));
                int end = json.indexOf("```", start);
                if (start > 0 && end > start) json = json.substring(start, end).trim();
            }
            int first = json.indexOf('{');
            int last = json.lastIndexOf('}');
            if (first >= 0 && last > first) {
                json = json.substring(first, last + 1);
            }
            Map<String, Object> parsed = mapper.readValue(json, new TypeReference<>() {});
            result.put("title", String.valueOf(parsed.getOrDefault("title", keyword)));
            result.put("type", String.valueOf(parsed.getOrDefault("type", "flowchart")));
            result.put("mermaid", String.valueOf(parsed.getOrDefault("mermaid", "")));
            result.put("description", String.valueOf(parsed.getOrDefault("description", "")));
        } catch (Exception e) {
            // 解析失败，尝试直接当mermaid代码用
            result.put("title", keyword);
            result.put("type", "flowchart");
            result.put("mermaid", raw);
            result.put("description", "");
        }
        return Result.ok(result);
    }
}
