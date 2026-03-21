package org.example.work.module.lessonplan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.work.module.lessonplan.dto.LessonPlanGenerateRequest;
import org.example.work.module.lessonplan.entity.LessonPlan;
import org.example.work.module.lessonplan.entity.LessonPlanVersion;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface LessonPlanService {

    /** AI 生成教案 */
    LessonPlan generate(LessonPlanGenerateRequest request, Long userId);

    /** AI 生成教案（流式） */
    void streamGenerate(LessonPlanGenerateRequest request, Long userId, SseEmitter emitter);

    /** 重新生成教案 */
    LessonPlan regenerate(Long id);

    /** 重新生成教案（流式） */
    void streamRegenerate(Long id, SseEmitter emitter);

    /** 分页查询 */
    Page<LessonPlan> page(int current, int size, Long userId, String keyword);

    /** 获取详情 */
    LessonPlan getById(Long id);

    /** 更新教案 */
    void update(LessonPlan lessonPlan);

    /** 删除教案 */
    void delete(Long id);

    /** 获取版本历史 */
    List<LessonPlanVersion> getVersions(Long lessonPlanId);
}
