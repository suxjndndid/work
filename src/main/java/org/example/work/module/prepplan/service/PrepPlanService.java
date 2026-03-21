package org.example.work.module.prepplan.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PrepPlanService {

    /** 生成总体备课方案 */
    String generatePrepPlan(Long lessonPlanId, Long courseId);

    /** 生成总体备课方案（流式） */
    void streamPrepPlan(Long lessonPlanId, Long courseId, SseEmitter emitter);
}
